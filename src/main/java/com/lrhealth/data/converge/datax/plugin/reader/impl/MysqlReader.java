package com.lrhealth.data.converge.datax.plugin.reader.impl;

import com.lrhealth.data.converge.datax.plugin.reader.AbstractReader;

/**
 * @author jinmengyu
 * @date 2023-08-31
 */
public class MysqlReader extends AbstractReader {
    @Override
    protected String getDataBase() {
        return "com.mysql.cj.jdbc.Driver";
    }

    @Override
    protected String getJdbcUrl(String host, String port, String databaseName) {
        // "jdbc:mysql://127.0.0.1:3306/database"
        return "jdbc:mysql://" + host + ":" + port + "/" + databaseName;
    }

    @Override
    protected String tableSql(String schemaName) {
        return "SELECT table_name FROM information_schema.tables" +
                "WHERE table_schema = '"+ schemaName + "';";
    }

    @Override
    protected String columnSql(String schemaName, String tableName) {
        return "select COLUMN_NAME from information_schema.COLUMNS where table_name = '" + tableName + "';";
    }

    @Override
    protected String readerQuerySql(String schemaName, String tableName, String condition) {
        return null;
    }
}
