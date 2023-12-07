package com.lrhealth.data.converge.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.lrhealth.data.converge.model.dto.CdcRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
 * CDC存储消费者
 *
 * @author yuanbaiyu
 * @since 2023/11/15 16:20
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "cdc.storage", havingValue = "true")
public class CdcStorageConsumer extends CdcCon {

    public final Pattern EXCEPTION_BATCH_UPDATE_DATA_TOO_LONG_FOR_COL = Pattern.compile("(?<=Data too long for column ').+(?=' at row)");
    private final String GROUP_ID = "storage";
    private final String ALTER_TABLE_COLUMN = "ALTER TABLE %s MODIFY %s VARCHAR2(%s);";
    private Connection connection;

    @Resource(name = "odsDataSource")
    private DataSource odsDataSource;

    @PostConstruct
    public void init() throws SQLException {
        connection = odsDataSource.getConnection();
    }

    @KafkaListener(topics = "${spring.kafka.topic.cdc}", groupId = GROUP_ID, containerFactory = "kafkaListenerContainerFactory")
    private void consumer(String message) throws Exception {
        List<CdcRecord> records = parseMessage(message, "insert", "update");

        if (CollUtil.isEmpty(records)) {
            return;
        }
        Map<String, List<CdcRecord>> map = new HashMap<>();
        for (CdcRecord cdcRecord : records) {
            String key = cdcRecord.getTable();
            List<CdcRecord> list = map.getOrDefault(key, Lists.newArrayList());
            list.add(cdcRecord);
            map.put(key, list);
        }
        generateBatchUpsertStatement(map);
    }

    // Generate and execute the BATCH INSERT statement
    private void generateBatchUpsertStatement(Map<String, List<CdcRecord>> map) throws Exception {
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

            StringBuilder updateClause = new StringBuilder();
            List<String> columnNames = new ArrayList<>(dataList.get(0).keySet());

            for (int i = 0; i < columnNames.size(); i++) {
                if (i > 0) {
                    updateClause.append(", ");
                }
                updateClause.append(columnNames.get(i)).append(" = VALUES(").append(columnNames.get(i)).append(")");
            }

            StringBuilder upsertQuery = new StringBuilder();
            upsertQuery.append("INSERT INTO ").append(tableName).append(" (");

            List<String> columns = new ArrayList<>(dataList.get(0).keySet());
            upsertQuery.append(StrUtil.join(", ", columns));

            upsertQuery.append(") VALUES (");
            String placeholders = columns.stream().map(column -> "?").collect(Collectors.joining(", "));
            upsertQuery.append(placeholders).append(")");
            upsertQuery.append(" ON DUPLICATE KEY UPDATE ");

            columns.forEach(column -> updateClause.append(", ").append(column).append(" = VALUES(").append(column).append(")"));

            upsertQuery.append(updateClause);

            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = connection.prepareStatement(upsertQuery.toString());
            } catch (SQLException e) {
                log.error("An exception occurred during prepare-statement. upsertQuery: {}", upsertQuery);
            }
            try {
                executePreparedStatement(preparedStatement, dataList);
            } catch (BatchUpdateException e) {
                exceptionHanding(e, fieldLenMap, tableName);
                executePreparedStatement(preparedStatement, dataList);
            } finally {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            }
        }
    }

    private boolean doesTableExist(String tableName, String schema) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        return metadata.getTables(null, schema, tableName, null).next();
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
            String alterTableFieldLengthSql = String.format(ALTER_TABLE_COLUMN, tableName, column, len);
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
            powerOfTwo <<= 1;
        }
        return powerOfTwo;
    }

}