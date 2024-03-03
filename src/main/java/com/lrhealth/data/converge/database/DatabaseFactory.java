package com.lrhealth.data.converge.database;


import cn.hutool.core.text.CharSequenceUtil;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.util.QueryParserUtil;
import com.lrhealth.data.converge.database.impl.*;


/**
 * @author jinmengyu
 * @date 2023-08-31
 */
public class DatabaseFactory {

    public DatabaseHandler getReader(String jdbcUrl, String dbUserName, String dbPassword){
        String dbType = QueryParserUtil.getDbType(jdbcUrl);
        if (CharSequenceUtil.isBlank(dbType)){
            throw new CommonException("无法识别的数据库连接: " + jdbcUrl);
        }
        return getDatabaseHandler(dbType, jdbcUrl, dbUserName, dbPassword);
    }

    private DatabaseHandler getDatabaseHandler(String dbType, String jdbcUrl, String dbUserName, String dbPassword){
        switch (dbType){
            case "postgresql":
                return new PostgresqlHandler(jdbcUrl, dbUserName, dbPassword, dbType);
            case "mysql":
                return new MysqlHandler(jdbcUrl, dbUserName, dbPassword, dbType);
            case "sqlserver":
                return new SqlserverHandler(jdbcUrl, dbUserName, dbPassword, dbType);
            case "oracle":
                return new OracleHandler(jdbcUrl, dbUserName, dbPassword, dbType);
            case "IRIS":
                return new CacheHandler(jdbcUrl, dbUserName, dbPassword, dbType);
            case "oceanbase":
                return new OceanbaseHandler(jdbcUrl, dbUserName, dbPassword, dbType);
            default:
                throw new CommonException("不支持的dataXReader");
        }
    }
}
