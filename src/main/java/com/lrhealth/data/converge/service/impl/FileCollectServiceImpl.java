package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.db.DbConnection;
import com.lrhealth.data.converge.common.db.DbConnectionManager;
import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import com.lrhealth.data.converge.common.enums.TunnelStatusEnum;
import com.lrhealth.data.converge.common.util.db.DbOperateUtils;
import com.lrhealth.data.converge.common.util.db.FieldInfo;
import com.lrhealth.data.converge.common.util.thread.AsyncFactory;
import com.lrhealth.data.converge.dao.entity.ConvDsConfig;
import com.lrhealth.data.converge.dao.entity.ConvTask;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.entity.StdOriginalModelColumn;
import com.lrhealth.data.converge.dao.service.*;
import com.lrhealth.data.converge.kafka.factory.KafkaConsumerContext;
import com.lrhealth.data.converge.kafka.factory.KafkaDynamicConsumerFactory;
import com.lrhealth.data.converge.model.dto.OriginalTableModelDto;
import com.lrhealth.data.converge.model.dto.TunnelMessageDTO;
import com.lrhealth.data.converge.service.FeTunnelConfigService;
import com.lrhealth.data.converge.service.FileCollectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class FileCollectServiceImpl implements FileCollectService {
    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private ConvTaskService taskService;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Resource
    private ConvOriginalTableService originalTableService;
    @Resource
    private ConvOdsDatasourceConfigService dsConfigService;
    @Resource
    private StdOriginalModelColumnService stdOriginalModelColumnService;
    @Resource
    private KafkaDynamicConsumerFactory dynamicConsumerFactory;
    @Resource
    private DbConnectionManager dbConnectionManager;
    @Resource
    private FeTunnelConfigService tunnelConfigService;

    @Resource
    private KafkaConsumerContext consumerContext;
    private static final String TOPIC_PREFIX = "file-collection-data-tunnelId-";
    private static final String TOPIC_KEY = "TOPIC_KEY";
    private static final String FILE_INTERFACE_GROUP_ID = "file_collete";

    @PostConstruct
    private void initialRegistry() {
        // 初始化文件采集消费者，如果是需要存到数据库，需要解析文件数据发到kafka
        List<ConvTunnel> queueTunnels = tunnelService.list(new LambdaQueryWrapper<ConvTunnel>()
                .eq(ConvTunnel::getConvergeMethod, TunnelCMEnum.FILE_MODE.getCode())
                .notIn(ConvTunnel::getStatus, TunnelStatusEnum.PAUSE.getValue(), TunnelStatusEnum.ABANDON.getValue())
                .ne(ConvTunnel::getDelFlag, 1));
        for (ConvTunnel convTunnel : queueTunnels) {
            TunnelMessageDTO tunnelMessage = tunnelConfigService.getTunnelMessage(convTunnel);
            if(tunnelMessage.getFileCollectInfoDto() != null && tunnelMessage.getFileCollectInfoDto().getFileStorageMode() == 1){
                initFileDataConsumer(convTunnel);
            }
        }
    }
    private void startConsumer(String broker, String topic, String topicKey) {
        KafkaConsumer<Object, Object> consumer = dynamicConsumerFactory.createConsumer(topic, FILE_INTERFACE_GROUP_ID, broker);
        log.info("文件采集kafka消费者创建成功！, consumer={}", consumer);
        consumerContext.addFileCollectConsumerTask(topicKey, consumer);
    }
    @Override
    public void initFileDataConsumer(ConvTunnel tunnel) {
        String topic = getTopic(tunnel);
        //创建唯一的topic，启动消费者监听
        String topicKey = TOPIC_KEY + topic;
        Integer status = tunnel.getStatus();
        switch (status) {
            case 1:
                ConvTask task = taskService.createTask(tunnel, false);
                startConsumer(bootstrapServers, topic, topicKey);
                AsyncFactory.convTaskLog(task.getId(), "消费者创建成功！");
                break;
            case 2:
                log.info("文件采集[{}]正在执行中，不重复添加创建！", tunnel.getId());
                break;
            case 3:
            case 4:
                consumerContext.removeConsumerTask(topicKey);
                break;
            default:
                log.error("status={}, 不是正常的管道状态", status);
        }
    }

    @Override
    public void fileDataHandler(String topicKey, List<String> values) {
        log.info("文件数据处理开始！, topicKey={}, 接收到数据：{} 条", topicKey, values.size());
        // 新增或查询task
        long tunnelId = Long.parseLong(topicKey.substring(topicKey.
                lastIndexOf(StrPool.DASHED) + 1));
        ConvTunnel tunnel = tunnelService.getById(tunnelId);
        //获取采集表，获取ods数据源id
        String table = tunnel.getCollectRange();
        ConvTask task = taskService.cdcFindTask(tunnel);
        AsyncFactory.convTaskLog(task.getId(), String.format("%s | table: %s获取记录%s条: ", DateUtil.now(),table,values.size()));
        OriginalTableModelDto tableModelRel = originalTableService.getTableModelRel(table, tunnel.getSysCode());
        log.info("获取到接口采集的表字段信息:{}", tableModelRel);
        List<StdOriginalModelColumn> tableColumns = stdOriginalModelColumnService.list
                (new LambdaQueryWrapper<StdOriginalModelColumn>()
                        .eq(StdOriginalModelColumn::getModelId, tableModelRel.getModelId())
                        .eq(StdOriginalModelColumn::getDelFlag, 0));
        List<FieldInfo> collect = tableColumns.stream().map(column -> {
            FieldInfo fieldInfo = new FieldInfo();
            fieldInfo.setFieldType(column.getFieldType());
            fieldInfo.setColumnName(column.getNameEn());
            fieldInfo.setPrimaryKey("1".equals(column.getPrimaryKeyFlag()));
            fieldInfo.setAutoIncrement(!"0".equals(column.getIncrFlag()));
            return fieldInfo;
        }).collect(Collectors.toList());
        ConvDsConfig datasourceConfig = dsConfigService.getById(tunnel.getWriterDatasourceId());
        // 获取连接
        DbConnection connection = DbConnection.builder()
                .dbUrl(datasourceConfig.getDsUrl())
                .dbUserName(datasourceConfig.getDsUsername())
                .dbPassword(datasourceConfig.getDsPwd())
                .dbDriver(datasourceConfig.getDsDriverName())
                .build();
        Connection conn = dbConnectionManager.getConnection(connection);
        try {
            DbOperateUtils.batchInsert(table, collect, JSON.parseArray(values.toString().toLowerCase()), conn, datasourceConfig.getDbType().toLowerCase());
            AsyncFactory.convTaskLog(task.getId(), String.format("%s | 文件采集方式： | table: %s插入记录%s条: ", DateUtil.now(),table,values.size()));
        } catch (Exception e) {
            log.error("文件数据处理错误！ error:{}", e.getMessage());
        }
    }
    private static String getTopic(ConvTunnel tunnel) {
        return TOPIC_PREFIX + tunnel.getId().toString();
    }

}
