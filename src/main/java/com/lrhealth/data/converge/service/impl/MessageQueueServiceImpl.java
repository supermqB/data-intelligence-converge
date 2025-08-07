package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharPool;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.db.DbConnection;
import com.lrhealth.data.converge.common.db.DbConnectionManager;
import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import com.lrhealth.data.converge.common.enums.TunnelStatusEnum;
import com.lrhealth.data.converge.common.exception.CommonException;
import com.lrhealth.data.converge.common.util.TemplateMakerUtil;
import com.lrhealth.data.converge.common.util.thread.AsyncFactory;
import com.lrhealth.data.converge.dao.entity.*;
import com.lrhealth.data.converge.dao.service.*;
import com.lrhealth.data.converge.kafka.factory.KafkaConsumerContext;
import com.lrhealth.data.converge.kafka.factory.KafkaDynamicConsumerFactory;
import com.lrhealth.data.converge.model.MessageParseFormat;
import com.lrhealth.data.converge.model.MessageSqlFormat;
import com.lrhealth.data.converge.model.dto.MessageParseDto;
import com.lrhealth.data.converge.model.dto.OpSqlDto;
import com.lrhealth.data.converge.model.dto.OriginalTableModelDto;
import com.lrhealth.data.converge.service.MessageQueueService;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jinmengyu
 * @date 2024-08-21
 */
@Service
@Slf4j
public class MessageQueueServiceImpl implements MessageQueueService {
    @Resource
    private MessageSqlFormat sqlTemplateFile;
    @Resource
    private MessageParseFormat parseFormat;
    @Resource
    private ConvMessageQueueConfigService queueConfigService;

    private static final String QUEUE_GROUP_ID = "queue_collect";

    private static final String CDC_GROUP_ID = "metrics";

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootStrap;

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
    private DbConnectionManager dbConnectionManager;

    private Properties sqlTemplateProp;

    @Resource
    private ConvTaskResultCdcService convTaskResultCdcService;

    /**
     * 启动时加载-sql的执行模板
     */
    @PostConstruct
    public void initParseSqlTemplate() {
        // 解析到外置文件的sql模板
        sqlTemplateProp = new Properties();
        log.info("从{}文件获取sql模板", sqlTemplateFile.getPath());
        try (InputStream in = new BufferedInputStream(Files.newInputStream(Paths.get(sqlTemplateFile.getPath())))) {
            sqlTemplateProp.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("模板文件解析失败，请修正文件!, exception={}", ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void queueModeCollect(ConvTunnel tunnel) {
        // 查询队列配置
        ConvMessageQueueConfig queueConfig = queueConfigService.getById(tunnel.getMessageQueueId());
        String topic = queueConfig.getKafkaTopic();
        log.info("打印队列配置:{}", queueConfig);
        String topicKey = tunnel.getId().toString() + CharPool.DASHED + topic;

        if (ObjectUtil.isNull(queueConfig) || !"kafka".equalsIgnoreCase(queueConfig.getQueueType())) {
            return;
        }
        Integer status = tunnel.getStatus();
        switch (status) {
            case 1:
                ConvTask task = taskService.createTask(tunnel, false);
                startConsumer(queueConfig.getKafkaBroker(), topic, topicKey, QUEUE_GROUP_ID);
                AsyncFactory.convTaskLog(task.getId(), "消费者创建成功！");
                break;
            case 2:
                log.info("队列采集[{}]正在执行中，不重复添加创建！", tunnel.getId());
                break;
            case 3:
            case 4:
                consumerContext.removeConsumerTask(topicKey);
                break;
            default:
                log.error("status={}, 不是正常的管道状态", status);
        }

    }

    private void startConsumer(String broker, String topic, String topicKey, String groupId) {
        KafkaConsumer<Object, Object> consumer = dynamicConsumerFactory.createConsumer(topic, groupId, broker);
        log.info("kafka消费者创建成功！, consumer={}", consumer);
        consumerContext.addConsumerTask(topicKey, consumer);
    }

    @Override
    public void messageQueueHandle(String topicKey, List<String> msgBody) {
        try {
            long tunnelId = Long.parseLong(topicKey.substring(0, topicKey.indexOf(StrPool.DASHED)));
            ConvTunnel tunnel = tunnelService.getById(tunnelId);
            // 新增或查询task
            ConvTask task = taskService.cdcFindTask(tunnel);
            // 同一个管道内采集的表，进行拆分
            Map<String, List<MessageParseDto>> tidyTableBodyMap = getTidyTableBodyMap(msgBody);

            for (Map.Entry<String, List<MessageParseDto>> tableBody : tidyTableBodyMap.entrySet()) {
                String table = tableBody.getKey();
                List<MessageParseDto> bodyList = tableBody.getValue();
                // 同一张表
                tableSqlSave(table, tunnel.getSysCode(), tunnel.getWriterDatasourceId(), bodyList, task.getId());
            }
        } catch (Exception e) {
            log.error("message queue exec error, {}", ExceptionUtils.getStackTrace(e));
        }
    }

    private Map<String, List<MessageParseDto>> getTidyTableBodyMap(List<String> msgBodyList) {
        Map<String, List<MessageParseDto>> result = new HashMap<>();

        for (String body : msgBodyList) {
            // 格式化value
            HashMap<String, Object> msgRecord = JSON.parseObject(body, HashMap.class);
            MessageParseDto valueParse = valueParse(msgRecord);
            log.info("解析后的消息体数据：{}", valueParse);
            String tableName = valueParse.getReadTable();
            result.computeIfAbsent(tableName, k -> new ArrayList<>()).add(valueParse);
        }
        return result;
    }

    private void tableSqlSave(String table, String sysCode, Integer writerDsId, List<MessageParseDto> parseDtoList,
            Integer taskId) {
        // 获取到落库配置
        OriginalTableModelDto tableModelRel = originalTableService.getTableModelRel(table, sysCode);
        log.info("获取到的表映射:{}", tableModelRel);
        ConvDsConfig datasourceConfig = dsConfigService.getById(writerDsId);

        // sql准备, 按照operation区分
        String preparedStatment = prepareSqlForInsert(parseDtoList, tableModelRel, datasourceConfig);
        Map<String, Integer> operationCount = new HashMap<>();
        StringBuilder operLog = new StringBuilder();

        Map<String, Long> opCountMap = parseDtoList.stream()
                .collect(Collectors.groupingBy(
                        MessageParseDto::getOperation, // 分组依据
                        Collectors.counting() // 统计数量
                ));
        opCountMap.forEach((op, count) -> operLog.append(getStandardOperation(op) + " ").append(count).append(" 条;"));
        // 获取连接
        DbConnection connection = DbConnection.builder()
                .dbUrl(datasourceConfig.getDsUrl())
                .dbUserName(datasourceConfig.getDsUsername())
                .dbPassword(datasourceConfig.getDsPwd())
                .dbDriver(datasourceConfig.getDsDriverName())
                .build();
        Connection conn = dbConnectionManager.getConnection(connection);

        // 执行sql
        doBatchInsert(datasourceConfig.getDsUrl().contains("hive2"), conn, preparedStatment,
                parseDtoList.stream().map(dto -> getValueMap(dto)).collect(Collectors.toList()));

        int totalCount = parseDtoList.size();

        AsyncFactory.convTaskLog(taskId,
                String.format("%s | table: %s获取记录%s条: ", DateUtil.now(), table, totalCount) + operLog);

        // 更新convTaskResultCdc
        ConvTaskResultCdc resultCdc = ConvTaskResultCdc.builder()
                .taskId(Long.valueOf(taskId))
                .tableName(table)
                .dataCount(Long.valueOf(totalCount))
                .addCount(opCountMap.getOrDefault("insert", Long.valueOf(0)))
                .updateCount(opCountMap.getOrDefault("update", Long.valueOf(0)))
                .deleteCount(opCountMap.getOrDefault("delete", Long.valueOf(0)))
                .build();
        convTaskResultCdcService.insertOrUpdateTaskResultCdc(resultCdc);
    }

    private void doBatchInsert(boolean isHive, Connection conn, String sql, List<Map<String, Object>> valuesList) {
        // 由于需要区分执行方式，所以具体值的填充在执行sql前进行，使得流程能够统一
        if (isHive) {
            // hive-jdbc不支持addBatch，使用拼接sql: insert into table (columns) values (?, ?, ?),
            // (?, ?, ?)
            batchByJoinSql(conn, sql, valuesList);
        } else {
            // 正常的addBatch, insert into table (columns) values (?, ?, ?)
            batchByPreparedStatement(conn, sql, valuesList);
        }
    }

    @Override
    public void cdcDbSaveQueue(ConvTunnel tunnel) {
        // cdc同步启动落库线程
        // topic格式：cdc-data-sysCode-tunnelId
        // 表达式填充
        Map<String, Object> propMap = new HashMap<>();
        propMap.put("sysCode", tunnel.getSysCode());
        propMap.put("tunnelId", tunnel.getId().toString());
        String topic;
        String cdcMessageTopicExpression = "CDC-DATA-${sysCode}-${tunnelId}";
        try {
            topic = TemplateMakerUtil.process(cdcMessageTopicExpression, propMap, null);
        } catch (TemplateException | IOException e) {
            throw new CommonException("cdc 消费者启动失败，根据表达式生成消费者topic失败，exp: " + cdcMessageTopicExpression);
        }
        String topicKey = tunnel.getId().toString() + CharPool.DASHED + topic;
        // 消费者启动
        startConsumer(kafkaBootStrap, topic, topicKey, CDC_GROUP_ID);
    }

    @PostConstruct
    private void initialRegistry() {
        // 队列采集
        List<ConvTunnel> queueTunnels = tunnelService.list(new LambdaQueryWrapper<ConvTunnel>()
                .eq(ConvTunnel::getConvergeMethod, TunnelCMEnum.QUEUE_MODE.getCode())
                .notIn(ConvTunnel::getStatus, TunnelStatusEnum.PAUSE.getValue(), TunnelStatusEnum.ABANDON.getValue())
                .ne(ConvTunnel::getDelFlag, 1));
        for (ConvTunnel convTunnel : queueTunnels) {
            queueModeCollect(convTunnel);
        }
        // cdc采集
        List<ConvTunnel> cdcTunnels = tunnelService.list(new LambdaQueryWrapper<ConvTunnel>()
                .eq(ConvTunnel::getConvergeMethod, TunnelCMEnum.CDC_LOG.getCode())
                .notIn(ConvTunnel::getStatus, TunnelStatusEnum.PAUSE.getValue(), TunnelStatusEnum.ABANDON.getValue())
                .ne(ConvTunnel::getDelFlag, 1));
        for (ConvTunnel convTunnel : cdcTunnels) {
            cdcDbSaveQueue(convTunnel);
        }
    }

    private MessageParseDto valueParse(Map<String, Object> recordValue) {
        // 操作
        String opKey = parseFormat.matchOperationKey(recordValue);
        String operation = CharSequenceUtil.isBlank(opKey) ? null : (String) recordValue.get(opKey);
        // 操作之前的值
        String preKey = parseFormat.matchPreValueKey(recordValue);
        Map<String, Object> preValue = CharSequenceUtil.isBlank(preKey) ? null
                : (Map<String, Object>) recordValue.get(preKey);
        // 操作之后的值
        String postKey = parseFormat.matchPostValueKey(recordValue);
        Map<String, Object> postValue = CharSequenceUtil.isBlank(postKey) ? null
                : (Map<String, Object>) recordValue.get(postKey);

        // 操作的表
        String tableKey = parseFormat.matchCollectTableKey(recordValue);
        String tableTopic = CharSequenceUtil.isBlank(tableKey) ? null : (String) recordValue.get(tableKey);

        return MessageParseDto.builder()
                .readTable(tableTopic)
                .operation(operation)
                .preValue(preValue)
                .postValue(postValue).build();
    }

    /**
     * 查询具体的sql模板，
     * 
     * @param parseDtoList
     * @param tableModelRel
     * @return Map<String, String> basic-batch sql、 management-sp sql
     * 
     *         @TODO, use batch insert instead;
     */
    private String prepareSqlForInsert(List<MessageParseDto> parseDtoList,
            OriginalTableModelDto tableModelRel, ConvDsConfig writerDs) {
        String sqlTemplate = getExecSql(tableModelRel);
        // 填充对应sql模板
        return sqlValueFill(sqlTemplate, getValueMap(parseDtoList.get(0)), tableModelRel.getModelName(),
                writerDs.getDbName());
    }

    @SuppressWarnings("null")
    private Map<String, Object> getValueMap(MessageParseDto valueParse) {
        String operation = valueParse.getOperation();
        Map<String, Object> valueMap = null;
        if (parseFormat.isInsertOp(operation)) {
            // sql模板
            valueMap = valueParse.getPostValue();
        }
        // 删除操作
        if (parseFormat.isDeleteOp(operation)) {
            valueMap = valueParse.getPreValue();
        }
        // 更新操作
        if (parseFormat.isUpdateOp(operation)) {
            valueMap = valueParse.getPostValue();
        }
        if (parseFormat.isManageOp(operation)) {
            return null;
        }
        /* 追加操作类型 */
        valueMap.put("operation", operation);

        return valueMap;
    }

    private String getExecSql(OriginalTableModelDto tableModelRel) {
        String template = spTemplate(sqlTemplateProp, tableModelRel);
        // 没有自定义sql，统一使用basic-insert
        return CharSequenceUtil.isBlank(template) ? basicTemplate(sqlTemplateProp) : template;
    }

    private String sqlValueFill(String sqlTemplate, Map<String, Object> sampleData, String tableName, String dbName) {
        List<String> columns = new LinkedList<>();
        Map<String, String> columnMap = new HashMap<>();
        List<String> values = new LinkedList<>();
        for (Map.Entry<String, Object> entry : sampleData.entrySet()) {
            String key = entry.getKey();
            columns.add(key);
            values.add("?");
        }
        columnMap.put("columns", CharSequenceUtil.join(",", columns));
        columnMap.put("value", CharSequenceUtil.join(",", values));
        columnMap.put("db", dbName);
        columnMap.put("table", tableName);
        // freemarker 填充
        String process = null;
        try {
            process = TemplateMakerUtil.process(sqlTemplate, columnMap, null);
        } catch (Exception e) {
            log.error("log error,{}", ExceptionUtils.getStackTrace(e));
        }
        return process;
    }

    /**
     * 查询对应的sql模板
     * dsId-odstablename.insert 数据源id+ods表名确定的sql语句
     */
    private String spTemplate(Properties prop, OriginalTableModelDto tableModelRel) {
        // 12-t_ds_co
        String dsTableId = tableModelRel.getModelDsConfigId().toString() + CharPool.DASHED
                + tableModelRel.getModelName();

        log.info("looking for SQL script of {}.", tableModelRel.getModelName());
        if (prop.containsKey(dsTableId)) {
            return (String) prop.get(dsTableId);
        }
        return null;
    }

    private String basicTemplate(Properties prop) {
        String prefix = "basic";
        if (prop.containsKey(prefix)) {
            return (String) prop.get(prefix);
        }
        return CharSequenceUtil.NULL;
    }

    private void batchByPreparedStatement(Connection conn, String sql, List<Map<String, Object>> valuesList) {
        if (CollUtil.isEmpty(valuesList))
            return;
        try (PreparedStatement stat = conn.prepareStatement(sql)) {
            for (Map<String, Object> valueMap : valuesList) {
                int i = 1;
                for (Map.Entry<String, Object> value : valueMap.entrySet()) {
                    stat.setObject(i++, value.getValue());
                }
                stat.addBatch();
            }
            stat.executeBatch();
        } catch (Exception e) {
            log.error("sql execute failed, [sql]={}", sql);
        }

    }

    private String replaceWithArray(String input, Object[] values) {
        if (input == null || values == null || values.length == 0) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        int valueIndex = 0; // 数组索引

        for (char c : input.toCharArray()) {
            if (c == '?') {
                Object val = values[valueIndex % values.length];
                if (val instanceof Integer) {
                    result.append(val);
                } else {
                    result.append("'" + val + "'");
                }

                valueIndex++;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    private void batchByJoinSql(Connection conn, String sql, List<Map<String, Object>> valuesList) {
        if (CollUtil.isEmpty(valuesList))
            return;
        Map<String, Object> firstValueMap = valuesList.get(0);
        // 比较模板中的？与实际的值能否对应
        long questionMask = sql.chars().filter(ch -> ch == '?').count();
        // ?数量应该等于map count -1,因为operation还没有加上去
        if (questionMask != firstValueMap.size()) {
            log.error("sql模板与实际值的数量无法对应上， sql模板={}， 实际的值={}", sql, firstValueMap);
        }

        String[] sqlSegs = sql.split("values");
        StringBuilder execSql = new StringBuilder(sqlSegs[0]);
        execSql.append(" values ");
        try (Statement stat = conn.createStatement()) {
            String valuesStr = valuesList.stream().map(valueMap -> {
                Object[] values = valueMap.values().toArray();
                return replaceWithArray(sqlSegs[1], values);
            }).collect(Collectors.joining(","));

            execSql.append(valuesStr);

            log.info("准备执行落库语句，sql={}", execSql);
            stat.execute(execSql.toString());
        } catch (Exception e) {
            log.error("sql execute failed, [sql]={}, message:{}", execSql, e.getMessage());
        }

    }

    private String getStandardOperation(String operation) {
        char o = operation.charAt(0);
        switch (o) {
            case 'c':
                return "insert";
            case 'd':
                return "delete";
            default:
                return "update";
        }
    }

}
