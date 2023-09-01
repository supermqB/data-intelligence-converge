package com.lrhealth.data.converge.datax.plugin.reader.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.lrhealth.data.converge.datax.plugin.reader.AbstractReader;

/**
 * @author jinmengyu
 * @date 2023-08-31
 */
public class PostgresqlReader extends AbstractReader {
    @Override
    protected String getDataBase() {
        return "org.postgresql.Driver";
    }

    @Override
    protected String getJdbcUrl(String host, String port, String databaseName) {
        // jdbc:postgresql://172.16.29.81:5432/rdcp_ext
        return "jdbc:postgresql://" + host + ":" + port + "/" + databaseName;
    }

    @Override
    protected String tableSql(String schemaName) {
        return "select tablename from pg_tables where schemaname='" + schemaName + "';";
    }

    @Override
    protected String columnSql(String schemaName, String tableName) {
        return "select column_name from information_schema.columns where table_schema='"
                + schemaName + "' and table_name='" + tableName + "';";
    }

    @Override
    protected String readerQuerySql(String schemaName, String tableName, String condition) {
        // "SELECT * FROM \"GY_HUIZHENDAN\";"
        String baseSql =  "select * from \\\"" + schemaName + "\\\".\\\"" + tableName + "\\\" ";
        if (CharSequenceUtil.isNotBlank(condition)){
            baseSql = baseSql + condition;
        }
        return baseSql;
    }
}
