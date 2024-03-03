package com.lrhealth.data.converge.database.impl;

import com.lrhealth.data.converge.common.util.JdbcUrlUtil;
import com.lrhealth.data.converge.common.util.QueryParserUtil;
import com.lrhealth.data.converge.database.DatabaseHandler;
import com.lrhealth.data.converge.datax.constant.DataXConstant;
import com.lrhealth.data.converge.model.vo.JdbcUrlMatchVo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

/**
 * cache数据库
 * @author jinmengyu
 * @date 2023-08-31
 */
@Slf4j
public class CacheHandler extends DatabaseHandler {

    // jdbc:IRIS://192.168.77.4:51773/dhc-app
    private static final String CACHE_URL_SAMPLE = "jdbc:IRIS://{host}[:{port}]/[{database}][\\?{params}]";

    public CacheHandler(String jdbcUrl, String dbUserName, String dbPassword, String databaseType) {
        super(jdbcUrl, dbUserName, dbPassword, databaseType);
    }


    @Override
    protected String getDataBase() {
        return "com.intersystems.jdbc.IRISDriver";
    }

    @Override
    protected JdbcUrlMatchVo matchJdbc(String jdbcUrl) {
        JdbcUrlMatchVo matchVo = new JdbcUrlMatchVo();
        Matcher matcher = JdbcUrlUtil.getPattern(CACHE_URL_SAMPLE)
                .matcher(jdbcUrl);
        if (matcher.matches()) {
            matchVo.setHost(matcher.group(DataXConstant.JDBC_HOST));
            matchVo.setPort(matcher.group(DataXConstant.JDBC_POST));
            matchVo.setDatabase(matcher.group(DataXConstant.JDBC_DATABASE));
            String params = matcher.group(DataXConstant.JDBC_PARAMS);
            if (null != params) {
                String[] pairs = params.split("&");
                List<String> pairList = new ArrayList<>();
                Collections.addAll(pairList, pairs);
                matchVo.setParamsList(pairList);
            }
        } else {
            log.error("error for cache jdbcUrl!");
        }
        return matchVo;
    }


    @Override
    protected String columnSql(String schemaName, String tableName) {
        return "select COLUMN_NAME from information_schema.COLUMNS where table_name = '" + tableName + "';";
    }

    @Override
    protected String handleRowId(String sqlQuery) {
        String s = QueryParserUtil.getTemplateQuery(sqlQuery);
        String dealQuery = " " + s.trim() + ", row_number() over (order by 1) as row_id ";
        return sqlQuery.replace(s, dealQuery);
    }

    @Override
    protected String handleXdsId(String sqlQuery, Long xdsId) {
        // select SELECT
        String s = QueryParserUtil.getTemplateQuery(sqlQuery);
        String dealQuery = " " + s.trim() + ", '" + xdsId + "' as xds_id ";
        return sqlQuery.replace(s, dealQuery);
    }

    @Override
    protected String getCountSql(String tableName, String schema) {
        return "select count(*) as count_number from " + tableName;
    }

    @Override
    protected String getStartIndex(String tableName, String seqField) {
        return "select min(" + seqField + ") from " + tableName + ";";
    }

    @Override
    protected String getEndIndex(String tableName, String seqField) {
        return "select max(" + seqField + ") from " + tableName + ";";
    }

}
