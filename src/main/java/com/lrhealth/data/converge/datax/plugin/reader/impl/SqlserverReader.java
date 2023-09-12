package com.lrhealth.data.converge.datax.plugin.reader.impl;

import com.lrhealth.data.converge.datax.plugin.reader.AbstractReader;

/**
 * @author jinmengyu
 * @date 2023-08-31
 */
public class SqlserverReader extends AbstractReader {
    @Override
    protected String getDataBase() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }

    @Override
    protected String getJdbcUrl(String host, String port, String databaseName) {
        return "jdbc:sqlserver://" + host + ":" + port + ";DatabaseName=" + databaseName;
    }

    @Override
    protected String tableSql(String schemaName) {
        return "SELECT name FROM sysobjects Where xtype='U'";
    }

    @Override
    protected String columnSql(String schemaName, String tableName) {
        return "SELECT * FROM syscolumns WHERE id=Object_Id('" + tableName + "')";
    }

    @Override
    protected String readerQuerySql(String schemaName, String tableName, String condition) {
        return null;
    }
}
