package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.text.CharPool;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.config.db.DataSourceRepository;
import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import com.lrhealth.data.converge.common.enums.TunnelStatusEnum;
import com.lrhealth.data.converge.common.util.TemplateMakerUtil;
import com.lrhealth.data.converge.common.util.thread.AsyncFactory;
import com.lrhealth.data.converge.dao.entity.ConvMessageQueueConfig;
import com.lrhealth.data.converge.dao.entity.ConvOdsDatasourceConfig;
import com.lrhealth.data.converge.dao.entity.ConvTask;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.service.*;
import com.lrhealth.data.converge.kafka.factory.KafkaConsumerContext;
import com.lrhealth.data.converge.kafka.factory.KafkaDynamicConsumerFactory;
import com.lrhealth.data.converge.model.MessageParseFormat;
import com.lrhealth.data.converge.model.dto.MessageParseDto;
import com.lrhealth.data.converge.model.dto.OriginalTableModelDto;
import com.lrhealth.data.converge.service.MessageQueueService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.teasoft.honey.osql.core.BeeFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author jinmengyu
 * @date 2024-08-21
 */
@Service
@Slf4j
public class MessageQueueServiceImpl implements MessageQueueService {
    @Value("${message.sql.path}")
    private String sqlTemplateFile;
    @Resource
    private MessageParseFormat parseFormat;
    @Resource
    private ConvMessageQueueConfigService queueConfigService;

    private static final String KAFKA_GROUP_ID = "queue_collect";

    @Resource
    private KafkaDynamicConsumerFactory dynamicConsumerFactory;
    @Resource
    private ConvTaskService taskService;
    @Resource
    private KafkaConsumerContext consumerContext;
    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private ConvOriginalTableService originalTableService;
    @Resource
    private ConvOdsDatasourceConfigService dsConfigService;
    @Resource
    private DataSourceRepository dataSourceRepository;

    private static Map<String,Integer> maxIdMap = new HashMap<>();

    @Override
    public void queueModeCollect(ConvTunnel tunnel) {
        // 查询队列配置
        ConvMessageQueueConfig queueConfig = queueConfigService.getById(tunnel.getMessageQueueId());
        log.info("打印队列配置:{}", queueConfig);
        if (ObjectUtil.isNull(queueConfig) || !"kafka".equalsIgnoreCase(queueConfig.getQueueType())){
            return;
        }
        ConvTask task = taskService.createTask(tunnel, false);
        String broker = queueConfig.getKafkaBroker();
        String topic = queueConfig.getKafkaTopic();
        KafkaConsumer<Object, Object> consumer = dynamicConsumerFactory.createConsumer(topic, KAFKA_GROUP_ID, broker);
        log.info("kafka消费者创建成功！, consumer={}", consumer);
        String topicKey = tunnel.getId().toString()  + CharPool.DASHED + topic;
        consumerContext.addConsumerTask(topicKey, consumer);
        AsyncFactory.convTaskLog(task.getId(), "消费者创建成功！");

    }

    @Override
    public void messageQueueHandle(String topicKey, String msgBody) {
        long tunnelId = Long.parseLong(topicKey.substring(0, topicKey.indexOf(StrPool.DASHED)));
        ConvTunnel tunnel = tunnelService.getById(tunnelId);
        String systemCode = tunnel.getSysCode();

        // 格式化value
        HashMap<String, Object> msgRecord = JSON.parseObject(msgBody, HashMap.class);
        MessageParseDto valueParse = valueParse(msgRecord);
        log.info("解析后的消息体数据：{}", valueParse);
        // 获取到落库配置
        OriginalTableModelDto tableModelRel = originalTableService.getTableModelRel(valueParse.getReadTable(), tunnel.getSysCode());
        log.info("获取到的表映射:{}", tableModelRel);
        ConvOdsDatasourceConfig datasourceConfig = dsConfigService.getById(tunnel.getWriterDatasourceId());

        // sql准备, 按照operation区分
        String sql = prepareSqlForOp(valueParse, tableModelRel);
        log.info("生成的完整sql语句：{}", sql);
        // 获取连接
        DataSource dataSource = BeeFactory.getInstance().getDataSourceMap().get(systemCode);
         if(ObjectUtil.isNull(dataSource)){
            dataSourceRepository.createDataSource(datasourceConfig);
            dataSource = BeeFactory.getInstance().getDataSourceMap().get(datasourceConfig.getId().toString());
        }
         // 执行sql
        executeSql(dataSource, sql);
    }




    @PostConstruct
    private void initialRegistry(){
        List<ConvTunnel> convTunnels = tunnelService.list(new LambdaQueryWrapper<ConvTunnel>()
                .eq(ConvTunnel::getConvergeMethod, TunnelCMEnum.QUEUE_MODE.getCode())
                .notIn(ConvTunnel::getStatus, TunnelStatusEnum.PAUSE.getValue(), TunnelStatusEnum.ABANDON.getValue())
                .ne(ConvTunnel::getDelFlag, 1));
        for (ConvTunnel convTunnel : convTunnels){
            queueModeCollect(convTunnel);
        }
    }

    private MessageParseDto valueParse(Map<String, Object> recordValue){
        // 操作
        String opKey = parseFormat.matchOperationKey(recordValue);
        String operation = CharSequenceUtil.isBlank(opKey) ? null : (String) recordValue.get(opKey);
        // 操作之前的值
        String preKey = parseFormat.matchPreValueKey(recordValue);
        Map<String, Object> preValue = CharSequenceUtil.isBlank(preKey) ? null : (Map<String, Object>) recordValue.get(preKey);
        // 操作之后的值
        String postKey = parseFormat.matchPostValueKey(recordValue);
        Map<String, Object> postValue = CharSequenceUtil.isBlank(postKey) ? null : (Map<String, Object>) recordValue.get(postKey);

        //
        String tableKey = parseFormat.matchCollectTableKey(recordValue);
        String tableTopic = CharSequenceUtil.isBlank(tableKey) ? null : (String) recordValue.get(tableKey);

        return MessageParseDto.builder()
                .readTable(tableTopic)
                .operation(operation)
                .preValue(preValue)
                .postValue(postValue).
                build();
    }

    private String prepareSqlForOp(MessageParseDto valueParse, OriginalTableModelDto tableModelRel) {
        String operation = valueParse.getOperation();
        // 解析到外置文件的sql模板
        Properties prop = new Properties();
        try(InputStream in = new BufferedInputStream(Files.newInputStream(Paths.get(sqlTemplateFile)))) {
            prop.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }catch (IOException e){
            log.error("模板文件解析失败，请修正文件!");
        }
        Map<String, String> sqlMap = null;
        Map<String, Object> valueMap = null;
        // 插入操作
        if (parseFormat.isInsertOp(operation)){
            // sql模板
            sqlMap = sqlTemplate(prop, "insert", tableModelRel);
            // 获取到字段键值
            valueMap = valueParse.getPostValue();
        }
        // 删除操作
        if (parseFormat.isDeleteOp(operation)){
            sqlMap = sqlTemplate(prop, "update", tableModelRel);
            valueMap = valueParse.getPostValue();
        }
        // 更新操作
        if (parseFormat.isUpdateOp(operation)){
            sqlMap = sqlTemplate(prop, "delete", tableModelRel);
            valueMap = valueParse.getPreValue();
        }
        log.info("获取到模板配置，sql={}", sqlMap);
        return sqlValueFill(sqlMap, valueMap, tableModelRel.getModelName());
    }

    private String sqlValueFill(Map<String, String> sqlMap, Map<String, Object> valueMap, String tableName){
        Map<String, Object> columnMap = new HashMap<>();
        String sqlTemplate;
        if (sqlMap.containsKey("basic")){
            sqlTemplate = sqlMap.get("basic");
            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();
            for (Map.Entry<String, Object> entry : valueMap.entrySet()){
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof String){
                    value = "'" + value + "'";
                }
                columns.append(key).append(StrPool.COMMA);
                values.append(value).append(StrPool.COMMA);
            }
            String columnResult = columns.substring(0, columns.length() - 1);
            String valueResult = values.substring(0, values.length() - 1);
            columnMap.put("columns", columnResult);
            columnMap.put("value", valueResult);
        }else{
            sqlTemplate = sqlMap.get("sp");
            for (Map.Entry<String, Object> entry : valueMap.entrySet()){
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof String){
                    value = "'" + value + "'";
                }
                columnMap.put(key, value);
            }
        }
        columnMap.put("table", tableName);
        try {
            return TemplateMakerUtil.process(sqlTemplate, columnMap, null);
        }catch (Exception e){
            log.error("log error,{}", ExceptionUtils.getStackTrace(e));
        }
        return sqlTemplate;
    }

    /**
     * 查询对应的sql模板
     * base. 基本的通用sql
     * sp.dsId-odstablename.insert 数据源id+ods表名确定的sql语句
     * @param prop
     * @param opKey
     * @param tableModelRel
     * @return
     */
    private Map<String, String> sqlTemplate(Properties prop, String opKey, OriginalTableModelDto tableModelRel){
        Map<String, String> res = new HashMap<>();
        //sp
        // 12-t_ds_co
        String code = tableModelRel.getModelDsConfigId().toString() + CharPool.DASHED + tableModelRel.getModelName();
        // sp.12-t_ds_co.insert
        String prefix = "sp" + CharPool.DOT + code + CharPool.DOT + opKey;
        if (prop.containsKey(prefix)){
            res.put("sp", (String) prop.get(prefix));
            return res;
        }
        // basic
        prefix = "basic" + CharPool.DOT + opKey;
        res.put("basic", (String) prop.get(prefix));
        return res;
    }

    private void executeSql(DataSource dataSource, String sql){
        try(Connection conn = dataSource.getConnection();
        Statement stat = conn.createStatement()) {
            stat.execute(sql);
        }catch (Exception e){
            log.error("sql execute failed, [sql]={}", sql);
        }
    }

    private static int getMaxId(String db, String tableName, Statement stmt){
        int maxId = Integer.MIN_VALUE;
        try{
            String target = db + "." + tableName;
            if (maxIdMap.containsKey(target)){
                maxId = maxIdMap.get(target) + 1;
            }else{
                String sql = String.format("select max(id) from %s.%s;", db, tableName);
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    String max_num = rs.getString(1);
                    if (max_num != null) {
                        maxId = rs.getInt(1) + 1;
                    }else{
                        maxId = 0;
                    }
                }
            }
            maxIdMap.put(target,maxId);
        }catch(Exception e){
            e.printStackTrace();
        }
        return maxId;
    }


}
