package com.lrhealth.data.converge.database.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.lrhealth.data.converge.common.util.JdbcUrlUtil;
import com.lrhealth.data.converge.common.util.QueryParserUtil;
import com.lrhealth.data.converge.database.DatabaseHandler;
import com.lrhealth.data.converge.datax.constant.DataXConstant;
import com.lrhealth.data.converge.model.vo.JdbcUrlMatchVo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author jinmengyu
 * @date 2023-08-31
 */
@Slf4j
public class PostgresqlHandler extends DatabaseHandler {

    private static final String POSTGRESQL_URL_SAMPLE = "jdbc:postgresql://{host}[:{port}]/[{database}][\\?{params}]";

    public PostgresqlHandler(String jdbcUrl, String dbUserName, String dbPassword, String databaseType) {
        super(jdbcUrl, dbUserName, dbPassword, databaseType);
    }


    @Override
    protected String getDataBase() {
        return "org.postgresql.Driver";
    }

    @Override
    protected JdbcUrlMatchVo matchJdbc(String jdbcUrl) {
        JdbcUrlMatchVo matchVo = new JdbcUrlMatchVo();
        Matcher matcher = JdbcUrlUtil.getPattern(POSTGRESQL_URL_SAMPLE)
                .matcher(jdbcUrl);
        if (matcher.matches()) {
            matchVo.setHost(matcher.group(DataXConstant.JDBC_HOST));
            matchVo.setPort(matcher.group(DataXConstant.JDBC_POST));
            matchVo.setDatabase(matcher.group(DataXConstant.JDBC_DATABASE));
            String params = matcher.group(DataXConstant.JDBC_PARAMS);
            if (null != params) {
                String[] pairs = params.split("&");
                List<String> pairList = new ArrayList<>();
                for (String pair : pairs) {
                    if (pair.contains(DataXConstant.JDBC_SCHEMA)){
                        matchVo.setSchemaName(pair.substring(pair.indexOf("=") + 1));
                        pairList.add(pair);
                    }
                }
                matchVo.setParamsList(pairList);
            }
        } else {
            log.error("error for postgresql jdbcUrl!");
        }
        return matchVo;
    }



    @Override
    protected String columnSql(String schemaName, String tableName) {
        if (CharSequenceUtil.isNotBlank(schemaName)){
            return "select column_name from information_schema.columns where table_schema='"
                    + schemaName + "' and table_name='" + tableName + "';";
        }else {
            return "select column_name from information_schema.columns where table_name='" + tableName + "';";
        }

    }

    @Override
    protected String handleRowId(String sqlQuery) {
        String s = QueryParserUtil.getTemplateQuery(sqlQuery);
        String dealQuery = " " + s.trim() + ", row_number() over (order by 1) as row_id ";
        return sqlQuery.replace(s, dealQuery);
    }


    @Override
    protected String getStartIndex(String tableName, String seqField) {
        return "select min(" + seqField + ") from \"" + tableName + "\";";
    }

    @Override
    protected String getEndIndex(String tableName, String seqField) {
        return "select max(" + seqField + ") from \"" + tableName + "\";";
    }

    @Override
    protected String handleXdsId(String sqlQuery, Long xdsId) {
        String s = QueryParserUtil.getTemplateQuery(sqlQuery);
        String dealQuery = " " + s.trim() + ", " + xdsId + " as xds_id ";
        return sqlQuery.replace(s, dealQuery);
    }

    @Override
    protected String getCountSql(String tableName, String schema) {
        String sql = "select count(*) as count_number from ";
        if (CharSequenceUtil.isNotBlank(schema)){
            return sql + schema + ".\"" + tableName + "\"";
        }
        return sql + "\"" + tableName + "\"";
    }
}
