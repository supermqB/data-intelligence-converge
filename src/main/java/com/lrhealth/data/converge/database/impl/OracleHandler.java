package com.lrhealth.data.converge.database.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.lrhealth.data.converge.common.util.JdbcUrlUtil;
import com.lrhealth.data.converge.common.util.QueryParserUtil;
import com.lrhealth.data.converge.database.DatabaseHandler;
import com.lrhealth.data.converge.datax.constant.DataXConstant;
import com.lrhealth.data.converge.model.vo.JdbcUrlMatchVo;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;

/**
 * @author jinmengyu
 * @date 2023-08-31
 */
@Slf4j
public class OracleHandler extends DatabaseHandler {

    private static final String ORACLE_SERVICE_URL_SAMPLE = "jdbc:oracle:thin:@//{host}[:{port}]/{serviceName}";

    private static final String ORACLE_SID_URL_SAMPLE = "jdbc:oracle:thin:@{host}[:{port}]:{sid}";

    public OracleHandler(String jdbcUrl, String dbUserName, String dbPassword, String databaseType) {
        super(jdbcUrl, dbUserName, dbPassword, databaseType);
    }


    @Override
    protected String getDataBase() {
        return "oracle.jdbc.OracleDriver";
    }

    @Override
    protected JdbcUrlMatchVo matchJdbc(String jdbcUrl) {
        JdbcUrlMatchVo matchVo = new JdbcUrlMatchVo();
        Matcher serviceMatcher = JdbcUrlUtil.getPattern(ORACLE_SERVICE_URL_SAMPLE).matcher(jdbcUrl);
        Matcher sidMatcher = JdbcUrlUtil.getPattern(ORACLE_SID_URL_SAMPLE).matcher(jdbcUrl);
        if (serviceMatcher.matches()) {
            matchVo.setHost(serviceMatcher.group(DataXConstant.JDBC_HOST));
            matchVo.setPort(serviceMatcher.group(DataXConstant.JDBC_POST));
            matchVo.setServiceName(serviceMatcher.group(DataXConstant.JDBC_ORACLE_SERVICE));
        }else if (sidMatcher.matches()){
            matchVo.setHost(sidMatcher.group(DataXConstant.JDBC_HOST));
            matchVo.setPort(sidMatcher.group(DataXConstant.JDBC_POST));
            matchVo.setSid(sidMatcher.group(DataXConstant.JDBC_ORACLE_SID));
        }else {
            log.error("error for oracle jdbcUrl!");
        }
        return matchVo;
    }


    @Override
    protected String columnSql(String schemaName, String tableName) {
        return "select COLUMN_NAME from all_tab_columns where Table_Name='" + tableName + "'";
    }

    @Override
    protected String handleRowId(String sqlQuery) {
        String s = QueryParserUtil.getTemplateQuery(sqlQuery);
        String dealQuery = " " + s.trim() + ", row_number() over (order by 1) as row_id ";
        return sqlQuery.replace(s, dealQuery);
    }

    @Override
    protected String getStartIndex(String tableName, String seqField) {
        return "select MIN(" + seqField + ") from " + tableName + ";";
    }

    @Override
    protected String getEndIndex(String tableName, String seqField) {
        return "select MAX(" + seqField + ") from " + tableName + ";";
    }

    @Override
    protected String handleXdsId(String sqlQuery, Long xdsId) {
        String s = QueryParserUtil.getTemplateQuery(sqlQuery);
        String dealQuery = " " + s.trim() + ", '" + xdsId + "' as xds_id ";
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
