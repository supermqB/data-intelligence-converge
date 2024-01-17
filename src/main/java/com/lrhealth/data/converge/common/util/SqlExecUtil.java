package com.lrhealth.data.converge.common.util;

import cn.hutool.log.Log;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.model.dto.DataSourceDto;
import com.lrhealth.data.model.enums.ResultEnum;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author jinmengyu
 * @date 2024-01-16
 */
public class SqlExecUtil {

    private static final Log log = Log.get(SqlExecUtil.class);


    public static List<Map<String, Object>> execSql(String sql, DataSourceDto dto) {
        List<Map<String, Object>> resultList;
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            Class.forName(dto.getDriver());
            connection = DriverManager.getConnection(dto.getJdbcUrl(), dto.getUsername(), dto.getPassword());
            if (null == connection) {
                throw new CommonException(ResultEnum.FAIL.getCode(), "get connection failed");
            }
            // sql has handled by sql parser with ? placeholder
            statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            resultList =  handleResultSet(resultSet);
        } catch (Exception ex) {
            log.error("test exec sql error:", ex);
            throw new CommonException("exec sql error");
        }finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            }catch (Exception e){
                log.error("statement close error: {}", ExceptionUtils.getStackTrace(e));
            }
            try {
                if (connection != null) {
                    connection.close();
                }
            }catch (Exception e){
                log.error("connection close error: {}", ExceptionUtils.getStackTrace(e));
            }
        }
        return resultList;
    }

    /**
     * 处理执行SQL后的结果
     *
     * @param resultSet jdbc的SQL执行结果
     * @return SQL的返回数据
     */
    private static List<Map<String, Object>> handleResultSet(ResultSet resultSet) throws SQLException {
        //暂存SQL的结果数据
        List<Map<String, Object>> dataList = new LinkedList<>();

        List<String> columns = selectColumns(resultSet);

        while (resultSet.next()) {
            Map<String, Object> rowData = new LinkedHashMap<>();
            for (String column : columns) {
                try {
                    Object value = resultSet.getObject(column);
                    rowData.put(column, value);
                } catch (SQLException ex) {
                    log.error(ex.getMessage());
                }
            }
            dataList.add(rowData);
        }
        return dataList;
    }

    private static List<String> selectColumns(ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsMetaData = resultSet.getMetaData();
        List<String> columns = new LinkedList<>();
        for (int i = 0; i < rsMetaData.getColumnCount(); i++) {
            String column = rsMetaData.getColumnLabel(i + 1);
            columns.add(column);
        }
        return columns;
    }

    public static void main(String[] args) {
        DataSourceDto dto = DataSourceDto.builder().driver("com.oceanbase.jdbc.Driver")
                .jdbcUrl("jdbc:oceanbase://172.16.29.68:2883/ods_test_gz?rewriteBatchedStatements=true")
                .username("root@rdcp_std")
                .password("LR_rdcp@2023")
                .build();
        String sql = "select data_converge_time from admission_record order by data_converge_time limit 1";
        execSql(sql, dto);
    }
}
