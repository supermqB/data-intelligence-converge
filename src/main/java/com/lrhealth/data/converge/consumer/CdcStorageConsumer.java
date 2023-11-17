package com.lrhealth.data.converge.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author yuanbaiyu
 * @Date 2023/11/15 16:20
 */
@Slf4j
@Component
public class CdcStorageConsumer extends CdcCon {

    public static final Pattern EXCEPTION_BATCH_UPDATE_DATA_TOO_LONG_FOR_COL = Pattern.compile("(?<=Data too long for column ').+(?=' at row)");
    private static final String GROUP_ID = "storage";
    private static final String alter_table_column = "ALTER TABLE %s MODIFY %s VARCHAR2(%s);";
    private static Connection connection;

    @Resource(name = "odsDataSource")
    private DataSource odsDataSource;

    @PostConstruct
    public void init() throws SQLException {
        connection = odsDataSource.getConnection();
    }

    @KafkaListener(topics = "${spring.kafka.topic.metrics}", groupId = GROUP_ID, containerFactory = "kafkaListenerContainerFactory")
    private void consumer(String message) throws SQLException {
        List<CdcRecord> records = parseMessage(message, "insert", "update");

        if (CollUtil.isEmpty(records)) {
            return;
        }
        Map<String, List<CdcRecord>> map = new HashMap<>();
        for (CdcRecord record : records) {
            String key = record.getTable();
            List<CdcRecord> list = map.getOrDefault(key, Lists.newArrayList());
            list.add(record);
            map.put(key, list);
        }
        generateBatchInsertStatement(map);
    }

    // Generate and execute the BATCH INSERT statement
    private void generateBatchInsertStatement(Map<String, List<CdcRecord>> map) throws SQLException {
        if (CollUtil.isEmpty(map)) {
            return;
        }
        for (Map.Entry<String, List<CdcRecord>> entry : map.entrySet()) {
            String tableName = entry.getKey();
            List<CdcRecord> records = entry.getValue();
            if (StrUtil.isBlank(tableName) || CollUtil.isEmpty(records)) {
                continue;
            }
            if (!doesTableExist(tableName, records.get(0).getSchema())) {
                log.error("Table [{}] does not exist.", tableName);
                continue;
            }
            List<TreeMap<String, Object>> dataList = records.stream().map(CdcRecord::getValue).collect(Collectors.toList());

            Map<String, Integer> fieldLenMap = new HashMap<>();

            for (TreeMap<String, Object> v : dataList) {
                v.entrySet().stream().filter(m -> m.getValue() instanceof String).forEach(m -> {
                    Integer len = fieldLenMap.getOrDefault(m.getKey(), 255);
                    fieldLenMap.put(m.getKey(), extendFieldLength(m.getValue(), len));
                });
            }

            // Prepare the batch INSERT statement using PreparedStatement
            PreparedStatement preparedStatement = generatePreparedStatement(connection, dataList.get(0), tableName);
            // Execute the batch INSERT statement
            try {
                executePreparedStatement(preparedStatement, dataList);
            } catch (BatchUpdateException e) {
                exceptionHanding(e, fieldLenMap, tableName);
                executePreparedStatement(preparedStatement, dataList);
            }
        }
    }

    private boolean doesTableExist(String tableName, String schema) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        return metadata.getTables(null, schema, tableName, null).next();
    }

    private PreparedStatement generatePreparedStatement(Connection connection, TreeMap<String, Object> data, String tableName) {
        StringBuilder insertQuery = new StringBuilder();
        StringBuilder valuesPlaceholder = new StringBuilder();

        insertQuery.append("INSERT INTO ").append(tableName).append(" (");
        valuesPlaceholder.append(") VALUES (");
        int parameterIndex = 1;
        for (Map.Entry<String, Object> columnEntry : data.entrySet()) {
            String columnName = columnEntry.getKey();

            if (parameterIndex > 1) {
                insertQuery.append(", ");
                valuesPlaceholder.append(", ");
            }

            insertQuery.append(columnName);
            valuesPlaceholder.append("?");
            parameterIndex++;
        }
        insertQuery.append(valuesPlaceholder).append(")");

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(insertQuery.toString());
        } catch (SQLException e) {
            log.error("An exception occurred during prepare-statement. insertQuery: {}", insertQuery);
        }
        return preparedStatement;
    }

    private void executePreparedStatement(PreparedStatement preparedStatement, List<TreeMap<String, Object>> dataList) throws SQLException {
        if (preparedStatement == null) {
            log.error("preparedStatement is null. data:{}", dataList.get(0));
            return;
        }
        TreeMap<String, Object> firstDataEntry = dataList.get(0);
        List<String> columnNames = new ArrayList<>(firstDataEntry.keySet());
        for (TreeMap<String, Object> dataEntry : dataList) {
            int parameterIndex = 1;
            for (String columnName : columnNames) {
                Object value = dataEntry.get(columnName);
                if (value != null) {
                    preparedStatement.setObject(parameterIndex, value);
                } else {
                    preparedStatement.setNull(parameterIndex, 0);
                }
                parameterIndex++;
            }
            preparedStatement.addBatch();
        }
        // Execute the batch INSERT statement
        preparedStatement.executeBatch();
    }

    private void exceptionHanding(BatchUpdateException e, Map<String, Integer> fieldLenMap, String tableName) throws SQLException {
        String msg = ExceptionUtil.getMessage(e);

        // java.sql.BatchUpdateException:Data truncation:(conn=98699)Data too long for column
        String column = ReUtil.getGroup0(EXCEPTION_BATCH_UPDATE_DATA_TOO_LONG_FOR_COL, msg);
        if (StrUtil.isNotBlank(column)) {
            Integer len = fieldLenMap.getOrDefault(column, 255 * 2);
            String alterTableFieldLengthSql = String.format(alter_table_column, tableName, column, len);
            connection.createStatement().execute(alterTableFieldLengthSql);
        }
    }

    private int extendFieldLength(Object value, int len) {
        int i = StrUtil.length(String.valueOf(value));
        return i > len ? findClosestPowerOfTwo(i) : len;
    }

    private int findClosestPowerOfTwo(int number) {
        if (number <= 0) {
            return 255;
        }
        int powerOfTwo = 1;
        while (powerOfTwo < number) {
            powerOfTwo <<= 1; // Equivalent to powerOfTwo *= 2;
        }
        return powerOfTwo;
    }

}
