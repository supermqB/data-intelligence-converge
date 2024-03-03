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
 * @author jinmengyu
 * @date 2023-08-31
 */
@Slf4j
public class MysqlHandler extends DatabaseHandler {

    private static final String MYSQL_URL_SAMPLE = "jdbc:mysql://{host}[:{port}]/[{database}][\\?{params}]";

    public MysqlHandler(String jdbcUrl, String dbUserName, String dbPassword, String databaseType) {
        super(jdbcUrl, dbUserName, dbPassword, databaseType);
    }


    @Override
    protected String getDataBase() {
        // 根据pom中mysql-connector-java决定驱动
        // mysql-connector-java6及以上 使用 com.mysql.cj.jdbc.Driver
        // mysql-connector-java5 使用 com.mysql.jdbc.Driver
        return "com.mysql.jdbc.Driver";
    }

    @Override
    protected JdbcUrlMatchVo matchJdbc(String jdbcUrl) {
        JdbcUrlMatchVo matchVo = new JdbcUrlMatchVo();
        Matcher matcher = JdbcUrlUtil.getPattern(MYSQL_URL_SAMPLE)
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
            log.error("error for mysql jdbcUrl!");
        }
        return matchVo;
    }


    @Override
    protected String columnSql(String schemaName, String tableName) {
        return "select COLUMN_NAME from information_schema.COLUMNS where table_name = '" + tableName + "';";
    }

    @Override
    protected String getStartIndex(String tableName, String seqField) {
        return "select min(" + seqField + ") from " + tableName + ";";
    }

    @Override
    protected String getEndIndex(String tableName, String seqField) {
        return "select max(" + seqField + ") from " + tableName + ";";
    }

    @Override
    protected String handleRowId(String sqlQuery) {
        String s = QueryParserUtil.getTemplateQuery(sqlQuery);
        // MYSQL中没有直接的随机整数，通过这种方式生成10亿以内的随机数
        String dealQuery = " " + s.trim() + ", ROUND(RAND() * 1000000000) as row_id ";
        return sqlQuery.replace(s, dealQuery);
    }

    @Override
    protected String handleXdsId(String sqlQuery, Long xdsId) {
        String s = QueryParserUtil.getTemplateQuery(sqlQuery);
        String dealQuery = " " + s.trim() + ", '" + xdsId + "' as xds_id ";
        return sqlQuery.replace(s, dealQuery);
    }

    @Override
    protected String getCountSql(String tableName, String schema) {
        return "select count(*) as count_number from " + tableName;
    }
}
