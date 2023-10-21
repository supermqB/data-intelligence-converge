package com.lrhealth.data.converge.datax.plugin.reader.impl;

import com.lrhealth.data.converge.datax.constant.DataXConstant;
import com.lrhealth.data.converge.datax.plugin.reader.AbstractReader;
import com.lrhealth.data.converge.model.vo.JdbcUrlMatchVo;
import com.lrhealth.data.converge.scheduled.utils.JdbcUrlUtil;
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
public class SqlserverReader extends AbstractReader {

    private static final String SQLSERVER_URL_SAMPLE = "jdbc:sqlserver://{host}[:{port}][;databaseName={database}][;{params}]";


    @Override
    protected String getDataBase() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }

    @Override
    protected JdbcUrlMatchVo matchJdbc(String jdbcUrl) {
        JdbcUrlMatchVo matchVo = new JdbcUrlMatchVo();

        Matcher matcher = JdbcUrlUtil.getPattern(SQLSERVER_URL_SAMPLE)
                .matcher(jdbcUrl);
        if (matcher.matches()) {
            matchVo.setHost(matcher.group(DataXConstant.JDBC_HOST));
            matchVo.setPort(matcher.group(DataXConstant.JDBC_POST));
            matchVo.setDatabase(matcher.group(DataXConstant.JDBC_DATABASE));
            String params = matcher.group(DataXConstant.JDBC_PARAMS);
            if (null != params) {
                String[] pairs = params.split(";");
                List<String> pairList = new ArrayList<>();
                Collections.addAll(pairList, pairs);
                matchVo.setParamsList(pairList);
            }
        } else {
            log.error("error for sqlserver jdbcUrl!");
        }
        return matchVo;
    }

    @Override
    protected String columnSql(String schemaName, String tableName) {
        return "SELECT * FROM syscolumns WHERE id=Object_Id('" + tableName + "')";
    }

    @Override
    protected String getStartIndex(String tableName, String seqField) {
        return "select MIN(" + seqField + ") from " + tableName + ";";
    }

    @Override
    protected String getEndIndex(String tableName, String seqField) {
        return "select MAX(" + seqField + ") from " + tableName + ";";
    }

}
