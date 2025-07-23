package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.text.CharPool;
import cn.hutool.core.text.StrPool;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.db.DbConnection;
import com.lrhealth.data.converge.common.db.DbConnectionManager;
import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import com.lrhealth.data.converge.common.enums.TunnelStatusEnum;
import com.lrhealth.data.converge.common.util.db.DbOperateUtils;
import com.lrhealth.data.converge.common.util.thread.AsyncFactory;
import com.lrhealth.data.converge.dao.entity.*;
import com.lrhealth.data.converge.dao.service.*;
import com.lrhealth.data.converge.kafka.factory.KafkaConsumerContext;
import com.lrhealth.data.converge.kafka.factory.KafkaDynamicConsumerFactory;
import com.lrhealth.data.converge.model.dto.OriginalTableModelDto;
import com.lrhealth.data.converge.service.ActiveInterfaceService;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ActiveInterfaceServiceImpl implements ActiveInterfaceService {
    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private ConvTaskService taskService;
    private static final String ACTIVE_INTERFACE_GROUP_ID = "active_interface";
    private static final String TOPIC_KEY = "TOPIC_KEY";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Resource
    private KafkaDynamicConsumerFactory dynamicConsumerFactory;
    @Resource
    private ConvOriginalTableService originalTableService;
    @Resource
    private ConvOdsDatasourceConfigService dsConfigService;
    @Resource
    private StdOriginalModelColumnService stdOriginalModelColumnService;
    private static final String TOPIC_PREFIX = "interface-collection-data-tunnelId-";
    @Resource
    private DbConnectionManager dbConnectionManager;
    @Resource
    private KafkaConsumerContext consumerContext;

    @PostConstruct
    private void initialRegistry() {
        // 初始化主动接口采集消费者
        List<ConvTunnel> queueTunnels = tunnelService.list(new LambdaQueryWrapper<ConvTunnel>()
                .eq(ConvTunnel::getConvergeMethod, TunnelCMEnum.ACTIVE_INTERFACE_MODE.getCode())
                .notIn(ConvTunnel::getStatus, TunnelStatusEnum.PAUSE.getValue(), TunnelStatusEnum.ABANDON.getValue())
                .ne(ConvTunnel::getDelFlag, 1));
        for (ConvTunnel convTunnel : queueTunnels) {
            initInterfaceDataConsumer(convTunnel);
        }
    }

    private void startConsumer(String broker, String topic, String topicKey) {
        KafkaConsumer<Object, Object> consumer = dynamicConsumerFactory.createConsumer(topic, ACTIVE_INTERFACE_GROUP_ID, broker);
        log.info("主动接口采集kafka消费者创建成功！, consumer={}", consumer);
        consumerContext.addActiveInterfaceConsumerTask(topicKey, consumer);
    }

    @Override
    public void activeInterfaceHandler(String topicKey, List<String> values) {
        log.info("主动接口数据处理开始！, topicKey={}, values={}", topicKey, values);
        if (Collections.isEmpty(values)) {
            return;
        }

        long tunnelId = Long.parseLong(topicKey.substring(topicKey.
                lastIndexOf(StrPool.DASHED) + 1));
        ConvTunnel tunnel = tunnelService.getById(tunnelId);
        //获取采集表，获取ods数据源id
        String table = tunnel.getCollectRange();
        OriginalTableModelDto tableModelRel = originalTableService.getTableModelRel(table, tunnel.getSysCode());
        log.info("获取到接口采集的表字段信息:{}", tableModelRel);
        List<StdOriginalModelColumn> tableColumns = stdOriginalModelColumnService.list
                (new LambdaQueryWrapper<StdOriginalModelColumn>()
                        .eq(StdOriginalModelColumn::getModelId, tableModelRel.getModelId())
                        .eq(StdOriginalModelColumn::getDelFlag, 0));
        ConvDsConfig datasourceConfig = dsConfigService.getById(tunnel.getWriterDatasourceId());
        //根据字段列表，组装insert语句
        String insertSql = assemblyInsertSql(tableColumns, table, values);
        log.info("接口采集拼接的最终insertSql={}", insertSql);
        try {
            //保存数据并，合并小文件。
            interfaceDataSave(table, datasourceConfig, insertSql);
        } catch (Exception e) {
            log.error("接口采集数据保存失败：{}", e.getMessage());
        }
    }

    private void interfaceDataSave(String table, ConvDsConfig datasourceConfig, String insertSql) throws Exception {
        DbConnection connection = DbConnection.builder()
                .dbUrl(datasourceConfig.getDsUrl())
                .dbUserName(datasourceConfig.getDsUsername())
                .dbPassword(datasourceConfig.getDsPwd())
                .dbDriver(datasourceConfig.getDsDriverName())
                .build();
        Connection conn = dbConnectionManager.getConnection(connection);
        DbOperateUtils.batchInsertHiveBySql(table, insertSql, conn, datasourceConfig.getDbIp(), true);
    }

    private String assemblyInsertSql(List<StdOriginalModelColumn> tableColumns, String table, List<String> dataList) {
        String templateSql = "insert into %s (%s)  values %s";
        StringBuilder execSql = new StringBuilder();
        String fields = tableColumns.stream().map(StdOriginalModelColumn::getNameEn).collect(Collectors.joining(","));
        List<JSONObject> data = dataList.stream().map(item -> JSONObject.parseObject(item, JSONObject.class)).collect(Collectors.toList());
        for (JSONObject item : data) {
            StringBuilder value = new StringBuilder();
            value.append("(");
            for (StdOriginalModelColumn column : tableColumns) {
                String fieldName = column.getNameEn();
                Object fieldValue = item.get(fieldName);
                if (fieldValue == null) {
                    value.append("null").append(CharPool.COMMA);
                } else {
                    value.append("'").append(fieldValue).append("'").append(CharPool.COMMA);
                }
            }
            value.deleteCharAt(value.length() - 1);
            value.append(")");
            //校验字段和参数的个数是否一致
            if (tableColumns.size() != value.toString().split(",").length) {
                log.error("字段和参数个数不一致！:{}", value);
            } else {
                execSql.append(value);
                execSql.append(",");
            }
        }
        execSql.deleteCharAt(execSql.length() - 1);
        return String.format(templateSql, table, fields, execSql);
    }

    private static String getTopic(ConvTunnel tunnel) {
        return TOPIC_PREFIX + tunnel.getId().toString();
    }

    @Override
    public void initInterfaceDataConsumer(ConvTunnel tunnel) {
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
                log.info("接口采集[{}]正在执行中，不重复添加创建！", tunnel.getId());
                break;
            case 3:
            case 4:
                consumerContext.removeConsumerTask(topicKey);
                break;
            default:
                log.error("status={}, 不是正常的管道状态", status);
        }

    }

}
