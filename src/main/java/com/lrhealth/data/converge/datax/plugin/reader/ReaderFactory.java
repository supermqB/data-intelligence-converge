package com.lrhealth.data.converge.datax.plugin.reader;

import com.lrhealth.data.converge.datax.plugin.reader.impl.MysqlReader;
import com.lrhealth.data.converge.datax.plugin.reader.impl.OracleReader;
import com.lrhealth.data.converge.datax.plugin.reader.impl.PostgresqlReader;
import com.lrhealth.data.converge.datax.plugin.reader.impl.SqlserverReader;

/**
 * @author jinmengyu
 * @date 2023-08-31
 */
public class ReaderFactory {

    public AbstractReader getReader(String database){
        switch (database){
            case "postgresql":
                return new PostgresqlReader();
            case "mysql":
                return new MysqlReader();
            case "sqlserver":
                return new SqlserverReader();
            case "oracle":
                return new OracleReader();
            default:
                return null;
        }
    }
}
