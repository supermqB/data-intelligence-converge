package com.lrhealth.data.converge.datax.plugin.reader.impl;

import com.lrhealth.data.converge.datax.plugin.reader.AbstractReader;

/**
 * @author jinmengyu
 * @date 2023-08-31
 */
public class OracleReader extends AbstractReader {
    @Override
    protected String getDataBase() {
        return "oracle.jdbc.OracleDriver";
    }

    @Override
    protected String getJdbcUrl(String host, String port, String databaseName) {
        return "jdbc:oracle:" + "thin:@" + host + ":" + port + ":" + databaseName;
    }

    @Override
    protected String tableSql(String schemaName) {
        return "select table_name from user_tables;";
    }

    @Override
    protected String columnSql(String schemaName, String tableName) {
        return "select * from user_tab_columns where Table_Name='" + tableName + "';";
    }

    @Override
    protected String readerQuerySql(String schemaName, String tableName, String condition) {
        return null;
    }
}
