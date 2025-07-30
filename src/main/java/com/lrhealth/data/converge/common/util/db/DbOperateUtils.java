package com.lrhealth.data.converge.common.util.db;


import java.sql.*;
import java.util.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbOperateUtils {
    /**
     * 批量插入数据到hive数据库,insertSql为拼接好的批量插入sql
     * @param tableName
     * @param insertSql
     * @param connection
     * @param dbType
     * @param mergeFiles
     * @throws Exception
     */
    public static void batchInsertHiveBySql(String tableName,String  insertSql, Connection connection,
                                            String dbType,Boolean mergeFiles) throws Exception {
        if ("hive".equalsIgnoreCase(dbType)) {
            executeHiveBatchInsert(connection, tableName, insertSql, mergeFiles);
        } else {
            log.error("batchInsertHiveBySql executed but datasource type not support");
            throw new Exception("batchInsertHiveBySql executed but datasource type not support");
        }
    }
    /***
     * 传入数据库连接信息，表名，字段名，json字符串，批量插入数据库
     * @param tableName
     * @param fields
     * @param jsonArray
     * @param connection
     * @param dbType
     * @throws Exception
     */
    public static void batchInsert(String tableName, List<FieldInfo> fields,
                                   JSONArray jsonArray, Connection connection,
                                   String dbType) throws Exception {
        if (jsonArray.isEmpty()) {
            log.error(" DbOperateUtils execute but No data to insert");
            return;
        }
        try {
            // Hive特殊处理
            if ("hive".equals(dbType)) {
                executeHiveBatchInsert(connection, tableName, fields, jsonArray);
            } else {
                // 其他数据库使用预编译语句
                String sql = buildInsertSql(tableName, fields);
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject jsonObj = jsonArray.getJSONObject(i);
                        setParameters(pstmt, fields, jsonObj);
                        pstmt.addBatch();
                        pstmt.executeBatch();
                    }
                }
            }

        } catch (SQLException e) {
            log.error(" DbOperateUtils batchInsert error:{}", e.getMessage());
            throw new Exception("Batch insert failed: " + e.getMessage());
        }
    }

    // 蛇形命名转驼峰命名
    private static String snakeToLowerCase(String snakeStr) {
        StringBuilder result = new StringBuilder();
        if (snakeStr == null || snakeStr.isEmpty()) {
            return "";
        }

        String[] parts = snakeStr.split("_");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (i == 0) {
                result.append(part.toLowerCase());
            } else {
                result.append(Character.toUpperCase(part.charAt(0)));
                result.append(part.substring(1).toLowerCase());
            }
        }
        return result.toString().toLowerCase();
    }

    // 构建插入SQL语句
    private static String buildInsertSql(String tableName, List<FieldInfo> fields) {
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();

        for (FieldInfo field : fields) {
            // 跳过自增主键
            if (field.isPrimaryKey() && field.isAutoIncrement()) {
                continue;
            }

            if (columns.length() > 0) {
                columns.append(", ");
                placeholders.append(", ");
            }
            columns.append(field.getColumnName());
            placeholders.append("?");
        }

        return String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName, columns, placeholders);
    }

    private static void setParameters(PreparedStatement pstmt, List<FieldInfo> fields,
                                      JSONObject jsonObj)
            throws SQLException {

        int paramIndex = 1;
        for (FieldInfo field : fields) {
            // 跳过自增主键
            if (field.isPrimaryKey() && field.isAutoIncrement()) {
                continue;
            }

            String columnName = field.getColumnName();
            String camelName = snakeToLowerCase(columnName);
            String fieldType = field.getFieldType();

            Object value = null;
            if (jsonObj.containsKey(camelName)) {
                value = jsonObj.get(camelName);
            }
            // 根据字段类型设置参数
            if (value == null || "null".equals(value)) {
                pstmt.setNull(paramIndex++, getSqlType(fieldType));
            } else if (fieldType.equalsIgnoreCase("int") || fieldType.equalsIgnoreCase("integer")) {
                pstmt.setInt(paramIndex++, jsonObj.getInteger(camelName));
            } else if (fieldType.equalsIgnoreCase("bigint")) {
                pstmt.setLong(paramIndex++, jsonObj.getLong(camelName));
            } else if (fieldType.equalsIgnoreCase("double")) {
                pstmt.setDouble(paramIndex++, jsonObj.getDouble(camelName));
            } else if (fieldType.equalsIgnoreCase("boolean")) {
                pstmt.setBoolean(paramIndex++, jsonObj.getBoolean(camelName));
            } else if (fieldType.equalsIgnoreCase("date")) {
                // 假设JSON中的日期是字符串格式，需要转换
                String dateStr = jsonObj.getString(camelName);
                pstmt.setDate(paramIndex++, java.sql.Date.valueOf(dateStr));
            } else {
                // 默认作为字符串处理
                pstmt.setString(paramIndex++, jsonObj.getString(camelName));
            }
        }
    }

    private static int getSqlType(String fieldType) {
        switch (fieldType.toLowerCase()) {
            case "int":
            case "integer":
                return Types.INTEGER;
            case "bigint":
                return Types.BIGINT;
            case "double":
                return Types.DOUBLE;
            case "boolean":
                return Types.BOOLEAN;
            case "date":
                return Types.DATE;
            default:
                return Types.VARCHAR;
        }
    }

    // Hive批量插入实现
    private static void executeHiveBatchInsert(Connection conn, String tableName,
                                               List<FieldInfo> fields, JSONArray jsonArray)
            throws SQLException {

        // 启用Hive的自动提交，因为Hive对事务支持有限

        // 构建Hive的INSERT VALUES语法
        StringBuilder columns = new StringBuilder();
        List<FieldInfo> insertFields = new ArrayList<>();

        // 过滤自增主键
        for (FieldInfo field : fields) {
            if (!field.isPrimaryKey() || !field.isAutoIncrement()) {
                insertFields.add(field);
                if (columns.length() > 0) columns.append(", ");
                columns.append(field.getColumnName());
            }
        }
        StringBuilder valuesClause = new StringBuilder();

        for (int i = 0; i < jsonArray.size(); i++) {
            if (i > 0) valuesClause.append(", ");
            valuesClause.append("(");
            JSONObject jsonObj = jsonArray.getJSONObject(i);
            for (int j = 0; j < insertFields.size(); j++) {
                if (j > 0) valuesClause.append(", ");

                FieldInfo field = insertFields.get(j);
                String columnName = field.getColumnName();
                String camelName = snakeToLowerCase(columnName);
                String fieldType = field.getFieldType();
                Object value = null;
                if (jsonObj.containsKey(camelName)) {
                    value = jsonObj.get(camelName);
                }

                if (value == null || "null".equals(value)) {
                    valuesClause.append("NULL");
                } else if (fieldType.equalsIgnoreCase("int") ||
                        fieldType.equalsIgnoreCase("integer") ||
                        fieldType.equalsIgnoreCase("bigint") ||
                        fieldType.equalsIgnoreCase("double") ||
                        fieldType.equalsIgnoreCase("boolean")) {
                    valuesClause.append(value.toString());
                } else if (fieldType.equalsIgnoreCase("date")) {
                    // Hive日期格式处理
                    String dateStr = value.toString();
                    valuesClause.append("'").append(dateStr).append("'");
                } else {
                    // 字符串类型需要加引号
                    String strValue = value.toString();
                    // 转义单引号
                    strValue = strValue.replace("'", "''");
                    valuesClause.append("'").append(strValue).append("'");
                }
            }

            valuesClause.append(")");
        }
        // 执行INSERT语句
        String sql = String.format("INSERT INTO %s (%s) VALUES %s",
                tableName, columns, valuesClause);

        executeHiveBatchInsert(conn, tableName, sql,false);
    }
    private static void executeHiveBatchInsert(Connection conn, String tableName, String sql, Boolean mergeFiles) throws SQLException {
        String mergeFile = String.format("alter table %s concatenate",
                tableName);
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            //合并小文件
            if(mergeFiles){
                stmt.execute(mergeFile);
            }
        }
    }
}

