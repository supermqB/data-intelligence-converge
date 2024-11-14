package com.lrhealth.data.converge.datax.plugin.reader;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.db.DbConnection;
import com.lrhealth.data.converge.common.db.DbConnectionManager;
import com.lrhealth.data.converge.common.util.QueryParserUtil;
import com.lrhealth.data.converge.common.util.TemplateMakerUtil;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.model.vo.JdbcUrlMatchVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jinmengyu
 * @date 2023-08-31
 */
@Slf4j
public abstract class AbstractReader {

    private final DbConnectionManager dbConnectionManager;

    protected AbstractReader(){
        this.dbConnectionManager = SpringUtil.getBean("com.lrhealth.data.converge.common.db.DbConnectionManager");
    }

    public void generateDatabaseReader(ConvTunnel tunnel, String tableName, String sqlQuery, String dataXJsonPath, String frontendFilePath){
        Connection con = getConnection(tunnel);
        try {
            boolean connectStatus = con.isValid(10);
            if (!connectStatus){
                log.error("数据库连接异常, 连接信息: {}, 用户名: {}, 密码: {}", tunnel.getJdbcUrl(), tunnel.getDbUserName(), tunnel.getDbPasswd());
                throw new CommonException("数据库连接异常，检查连接信息");
            }
            List<String> columnList = new ArrayList<>();
            if (CharSequenceUtil.isBlank(sqlQuery)){
                sqlQuery = "select * from " + tableName;
            }
            if (sqlQuery.contains("*")){
                try (Statement statement = con.createStatement()){
                    JdbcUrlMatchVo matchVo = matchJdbc(tunnel.getJdbcUrl());
                    String columnSql = columnSql(matchVo.getSchemaName() != null ? matchVo.getSchemaName() : null, tableName);
                    ResultSet resultSet = statement.executeQuery(columnSql);
                    while(resultSet.next()) {
                        String columnName = resultSet.getString(1);
                        columnList.add(columnName);
                    }
                    String collect = columnList.stream().map(s -> "\\\"" + s + "\\\"").collect(Collectors.joining(","));
                    sqlQuery = sqlQuery.replace("*", collect);
                    log.info("更新之后的采集语句: {}", sqlQuery);
                }catch (Exception e){
                    log.error("log error,{}", ExceptionUtils.getStackTrace(e));
                }
            }else {
                columnList = QueryParserUtil.queryColumnParser(sqlQuery);
            }
            // 开始生成json
            createDataXJson(tableName, sqlQuery, tunnel, dataXJsonPath, frontendFilePath, columnList);
        } catch (Exception e) {
            log.error("log error,{}", ExceptionUtils.getStackTrace(e));
        }
    }

    private Connection getConnection(ConvTunnel tunnel){
        DbConnection dbConnection = DbConnection.builder().dbUrl(tunnel.getJdbcUrl())
                .dbUserName(tunnel.getDbUserName())
                .dbPassword(tunnel.getDbPasswd())
                .dbDriver(getDataBase())
                .build();
        return dbConnectionManager.getConnection(dbConnection);
    }


    public String readerTableSeqFieldIndex(ConvTunnel tunnel, String tableName, String seqField, String index){
        Connection con = getConnection(tunnel);
        try (Statement statement = con.createStatement()){
            String queryIndex;
            if (index.equals("startIndex")){
                queryIndex = getStartIndex(tableName, seqField);
            }else {
                queryIndex = getEndIndex(tableName, seqField);
            }
            ResultSet resultSet = statement.executeQuery(queryIndex);
            String endIndex = null;
            while(resultSet.next()) {
                endIndex = resultSet.getString(1);
            }
            return endIndex;
        }catch (Exception e){
            log.error("log error,{}", ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    private void createDataXJson(String table, String sqlQuery, ConvTunnel tunnel, String dataXJsonPath, String frontendFilePath, List<String> columnList){
        try {
            TemplateMakerUtil makerUtil = new TemplateMakerUtil();
            makerUtil.init();
            Map<String, Object> podMap = new HashMap<>();
            podMap.put("dbType", QueryParserUtil.getDbType(tunnel.getJdbcUrl()));
            podMap.put("dbUserName", tunnel.getDbUserName());
            podMap.put("dbPasswd", tunnel.getDbPasswd());
            podMap.put("sqlQuery", sqlQuery);
            podMap.put("jdbcUrl", tunnel.getJdbcUrl());
            podMap.put("frontendFilePath", frontendFilePath);
            podMap.put("table", table);
            log.info("(tunnel-datax)table: {}, columnSize: {}", table, columnList.size());
            String collect = columnList.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(","));
            podMap.put("columnHeader", collect);
            log.info("***开始生成文件!***");
            makerUtil.process(podMap, dataXJsonPath);
        } catch (Exception e) {
            log.error("log error,{}", ExceptionUtils.getStackTrace(e));
        }
    }

    protected abstract String getStartIndex(String tableName, String seqField);
    protected abstract String getEndIndex(String tableName, String seqField);
    protected abstract String getDataBase();
    protected abstract JdbcUrlMatchVo matchJdbc(String jdbcUrl);
    protected abstract String columnSql(String schemaName, String tableName);


}
