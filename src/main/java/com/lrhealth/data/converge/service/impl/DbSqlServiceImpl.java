package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.lrhealth.data.converge.common.db.DbConnection;
import com.lrhealth.data.converge.common.db.DbConnectionManager;
import com.lrhealth.data.converge.common.enums.OdsDataSizeEnum;
import com.lrhealth.data.converge.common.util.QueryParserUtil;
import com.lrhealth.data.converge.model.bo.ColumnDbBo;
import com.lrhealth.data.converge.model.dto.DataSourceDto;
import com.lrhealth.data.converge.service.DbSqlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.teasoft.honey.osql.core.ExceptionHelper;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author jinmengyu
 * @date 2023-10-24
 */
@Slf4j
@Service
public class DbSqlServiceImpl implements DbSqlService {
    @Resource
    private DbConnectionManager dbConnectionManager;

    @Override
    public void createTable(List<ColumnDbBo> header, String odsTableName, DataSourceDto dataSourceDto) {
        StringBuilder createSql = new StringBuilder("CREATE TABLE " + odsTableName + " (\n");
        StringBuilder columnSql = new StringBuilder();
        for (ColumnDbBo modelColumn : header){
            // 字段名称
            columnSql.append(modelColumn.getColumnName()).append(" ");
            // 字段类型
            if (modelColumn.getFieldLength() != null && CharSequenceUtil.isNotBlank(modelColumn.getFieldType())) {
                columnSql.append(modelColumn.getFieldType()).append("(")
                        .append(modelColumn.getFieldLength()).append(") ");
            } else if (CharSequenceUtil.isNotBlank(modelColumn.getFieldType()) && modelColumn.getFieldType().equals("varchar")){
                columnSql.append(modelColumn.getFieldType()).append("(")
                        .append(255).append(") ");
            }else if (CharSequenceUtil.isNotBlank(modelColumn.getFieldType())){
                columnSql.append(modelColumn.getFieldType()).append(" ");
            }else {
                columnSql.append("varchar(255)").append(" ");
            }
            columnSql.append("DEFAULT NULL,").append("\n");
        }
        //xds_id和row_id
        columnSql.append("xds_id bigint(20) NOT NULL,").append("\n");
        columnSql.append("row_id bigint(20) NOT NULL AUTO_INCREMENT,").append("\n");
        columnSql.append("KEY ").append(odsTableName).append("_idx1 ").append("(row_id) LOCAL").append("\n");
        createSql.append(columnSql).append(") ")
                .append("AUTO_INCREMENT = 0 AUTO_INCREMENT_MODE = 'ORDER' DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC ")
                .append("COMPRESSION = 'zstd_1.3.8' REPLICA_NUM = 3 BLOCK_SIZE = 16384 USE_BLOOM_FILTER = FALSE TABLET_SIZE = 134217728 PCTFREE = 0;");


        log.info("table [{}]  sql:[{}]", odsTableName, createSql);
        execSql(String.valueOf(createSql), dataSourceDto);
    }

    @Override
    public boolean checkOdsTableExist(String odsTableName, DataSourceDto dataSourceDto) {
        String dbSchema = QueryParserUtil.getDbSchema(dataSourceDto.getJdbcUrl());
        String checkSql;
        if (CharSequenceUtil.isNotBlank(dbSchema)){
            checkSql = "select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '" + odsTableName + "' and TABLE_SCHEMA = '" + dbSchema + "';";
        }else {
            checkSql = "select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '" + odsTableName + "';";
        }
        String result = execSql(checkSql, dataSourceDto);
        return (result != null);
    }

    @Override
    public Long getAvgRowLength(String odsTableName, DataSourceDto dataSourceDto, String odsModelName){
        // 刷新配置,只需要执行一次
        // todo: ALTER SYSTEM SET ENABLE_SQL_EXTENSION = TRUE;
        String result = null;
        // 获取每行的平均大小
        String selectSql = "select AVG_ROW_LENGTH from information_schema.TABLES where TABLE_NAME = '" + odsTableName + "';";
        try {
            result = execSql(selectSql, dataSourceDto);
        }catch (Exception e){
            log.error("获取数据库表[{}]容量失败：{}", odsModelName, ExceptionUtils.getStackTrace(e));
        }

        // todo: 暂时给一个默认值
        if (CharSequenceUtil.isBlank(result) || result.equals("0")){
            return OdsDataSizeEnum.getValue(odsModelName);
        }
        return Long.parseLong(result);
    }

    @Override
    public String execSql(String sql, DataSourceDto dataSourceDto) {
        ResultSet rs = null;
        String execSql = deleteLastSemicolon(sql);
        DbConnection dbConnection = DbConnection.builder().dbUrl(dataSourceDto.getJdbcUrl())
                .dbUserName(dataSourceDto.getUsername())
                .dbPassword(dataSourceDto.getPassword())
                .dbDriver(dataSourceDto.getDriver())
                .build();
        Connection conn = dbConnectionManager.getConnection(dbConnection);
        try (PreparedStatement pst = conn.prepareStatement(execSql)){
            rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            throw ExceptionHelper.convert(e);
        }finally {
            if(rs!=null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private String deleteLastSemicolon(String sql) {
        String new_sql = sql.trim();
        if (new_sql.endsWith(";")) return new_sql.substring(0, new_sql.length() - 1); //fix oracle ORA-00911 bug.oracle用jdbc不能有分号
        return sql;
    }


}
