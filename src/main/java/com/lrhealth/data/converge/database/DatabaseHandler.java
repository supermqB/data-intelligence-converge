package com.lrhealth.data.converge.database;

import cn.hutool.core.collection.CollUtil;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.enums.DatabaseCheckEnum;
import com.lrhealth.data.converge.common.util.QueryParserUtil;
import com.lrhealth.data.converge.model.dto.ColumnInfoDTO;
import com.lrhealth.data.converge.model.dto.OriginalTableDto;
import com.lrhealth.data.converge.model.vo.JdbcUrlMatchVo;
import com.lrhealth.data.model.enums.ResultEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.sql.*;
import java.util.*;

/**
 * @author jinmengyu
 * @date 2024-01-02
 */
@Slf4j
@Data
public abstract class DatabaseHandler {

    private final String jdbcUrl;

    private final String dbUserName;

    private final String dbPassword;

    private final String databaseType;


    protected DatabaseHandler(String jdbcUrl, String dbUserName, String dbPassword, String databaseType){
        this.jdbcUrl = jdbcUrl;
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
        this.databaseType = databaseType;
    }

    public Connection getConnection(){
        try {
            Class.forName(getDataBase());
            return DriverManager.getConnection(jdbcUrl, dbUserName, dbPassword);
        } catch (Exception e) {
            log.error("get connection error:", e);
        }
        return null;
    }

    public boolean testConnection() throws ClassNotFoundException {
        Class.forName(getDataBase());
        String sql = DatabaseCheckEnum.getCheckSql(QueryParserUtil.getDbType(jdbcUrl));
        //mysql sqlserver pg三种数据库目前都支持select 1的形式来测试链接 oracle oceanbase 支持 select 1 from dual
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUserName, dbPassword);
             Statement stat = conn.createStatement();
             ResultSet rs = stat.executeQuery(sql)) {
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            log.error("test connection error: msg = {}", e.getMessage());
        }
        return false;
    }


    public List<Map<String, Object>> execSql(String sql) {
        List<Map<String, Object>> resultList;
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            if (null == connection) {
                throw new CommonException(ResultEnum.FAIL.getCode(), "get connection failed");
            }
            // sql has handled by sql parser with ? placeholder
            statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            resultList =  handleResultSet(resultSet);
        } catch (SQLException ex) {
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
    private List<Map<String, Object>> handleResultSet(ResultSet resultSet) throws SQLException {
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

    private List<String> selectColumns(ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsMetaData = resultSet.getMetaData();
        List<String> columns = new LinkedList<>();
        for (int i = 0; i < rsMetaData.getColumnCount(); i++) {
            String column = rsMetaData.getColumnLabel(i + 1);
            columns.add(column);
        }
        return columns;
    }


    public List<OriginalTableDto> getTablesDetails(String schema, Integer structure) {
        List<OriginalTableDto> originalTables = new ArrayList<>();
        Connection con = null;
        try {
            con = getConnection();
            if (null == con) {
                throw new CommonException(ResultEnum.FAIL.getCode(), "get connection failed");
            }
            DatabaseMetaData metaData = con.getMetaData();
            JdbcUrlMatchVo jdbcUrlMatchVo = matchJdbc(jdbcUrl);
            String database = jdbcUrlMatchVo.getDatabase();
            //检索数据库中的列
            getTableDetails(originalTables, metaData, database, schema, structure);
            if (CollUtil.isEmpty(originalTables) && databaseType.equals("oracle")){
                schema = dbUserName;
                getTableDetails(originalTables, metaData, database, schema, structure);
            }
            if (CollUtil.isEmpty(originalTables) && databaseType.equals("oracle")){
                schema = dbUserName.toUpperCase();
                getTableDetails(originalTables, metaData, database, schema, structure);
            }
        } catch(SQLException e){
            log.error("get tables error:", e);
        }finally {
            try {
                if (con != null) {
                    con.close();
                }
            }catch (Exception e){
                log.error("connection close error: {}", ExceptionUtils.getStackTrace(e));
            }
        }
        return originalTables;
    }

    private void getTableDetails(List<OriginalTableDto> originalTables, DatabaseMetaData metaData, String database, String schema, Integer structure) throws SQLException {
        String[] types;
        if (structure == 2){
            types = new String[]{"VIEW"};
        }else {
            types = new String[]{"TABLE"};
        }
        ResultSet rs = metaData.getTables(database, schema, "%", types);
        while (rs.next()) {
            String tableName = rs.getString("TABLE_NAME");
            String tableRemarks = rs.getString("REMARKS");
            OriginalTableDto tableDto = OriginalTableDto.builder().tableName(tableName).tableRemarks(tableRemarks).build();
            originalTables.add(tableDto);
        }
    }

    public List<ColumnInfoDTO> getColumnDetails(String tableName, String schema) {
        List<ColumnInfoDTO> columnInfoDTOList = new ArrayList<>();
        Connection con = null;
        try {
            con = getConnection();
            if (null == con) {
                throw new CommonException(ResultEnum.FAIL.getCode(), "get connection failed");
            }
            Map<String, Short> primaryKeyMap = new HashMap<>();
            JdbcUrlMatchVo matchVo = matchJdbc(jdbcUrl);
                // 获取主键
                DatabaseMetaData metaData = con.getMetaData();
                ResultSet primaryKeys = metaData.getPrimaryKeys(matchVo.getDatabase(), schema, tableName);
                while (primaryKeys.next()) {
                    String columnName = primaryKeys.getString("COLUMN_NAME");
                    short keySeq = primaryKeys.getShort("KEY_SEQ");
                    primaryKeyMap.put(columnName, keySeq);
                }
                //检索数据库中的列
                ResultSet rs = metaData.getColumns(matchVo.getDatabase(), schema, tableName, "%");
                while (rs.next()) {
                    String colName = rs.getString("COLUMN_NAME");
                    String remarks = rs.getString("REMARKS");
                    String typeName = rs.getString("TYPE_NAME");
                    Integer colLength = rs.getInt("COLUMN_SIZE");
                    String columnDef = rs.getString("COLUMN_DEF");
                    int nullable = rs.getInt("NULLABLE");
                    // nullable的结果0-必填；1-非必填，数据库中时0-非必填；1必填。进行翻转
                    nullable = (nullable == 0) ? 1 : 0;
                    ColumnInfoDTO columnInfoDTO = ColumnInfoDTO.builder()
                            .columnName(colName).columnTypeName(typeName).remark(remarks)
                            .columnLength(colLength).columnDef(columnDef).nullable(nullable)
                            .primaryKeyFlag(0)
                            .build();
                    if (primaryKeyMap.containsKey(colName)){
                        columnInfoDTO.setPrimaryKeyFlag(1);
                        columnInfoDTO.setPrimaryKeySeq(Integer.valueOf(primaryKeyMap.get(colName)));
                    }
                    columnInfoDTOList.add(columnInfoDTO);
                }
        } catch (SQLException e) {
            log.error("test get column error:", e);
        }finally {
            try {
                if (con != null) {
                    con.close();
                }
            }catch (Exception e){
                log.error("connection close error: {}", ExceptionUtils.getStackTrace(e));
            }
        }
        return columnInfoDTOList;
    }

    public String getJdbcUrl(){
        return jdbcUrl;
    }

    public String getSqlQuery(String tableName){
        JdbcUrlMatchVo matchVo = matchJdbc(jdbcUrl);
        return columnSql(matchVo.getSchemaName() != null ? matchVo.getSchemaName() : null, tableName);
    }

    public String readerTableSeqFieldIndex(String tableName, String seqFields, String index){
        Statement statement = null;
        try (Connection con = getConnection();){
            statement = con.createStatement();
            String queryIndex;
            if (index.equals("startIndex")){
                queryIndex = getStartIndex(tableName, seqFields);
            }else {
                queryIndex = getEndIndex(tableName, seqFields);
            }
            log.info("索引执行的sql语句：{}", queryIndex);
            ResultSet resultSet = statement.executeQuery(queryIndex);
            String value = null;
            while(resultSet.next()) {
                value = resultSet.getString(1);
            }
            return value;
        }catch (Exception e){
            log.error("log error,{}", ExceptionUtils.getStackTrace(e));
        }finally {
            if(statement!=null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public Object getTableCount(String tableName, String schema){
        String sql = getCountSql(tableName, schema);
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            result = execSql(sql);
        }catch (Exception e){
            log.error("[{}] table get count error: {}", tableName, ExceptionUtils.getStackTrace(e));
        }
        if (CollUtil.isEmpty(result)){
            return null;
        }
        // 不同数据库会有大写或小写countNumber情况
        Map<String, Object> map = result.get(0);
        if (map.containsKey("count_number")){
            return map.get("count_number");
        }
        return map.get("COUNT_NUMBER");
    }

    public String addXdsId(String sqlQuery, Long xdsId){
        return handleXdsId(sqlQuery, xdsId);
    }

    public String addRowId(String sqlQuery){
        return handleRowId(sqlQuery);
    }


    protected abstract String getDataBase();
    protected abstract JdbcUrlMatchVo matchJdbc(String jdbcUrl);
    protected abstract String columnSql(String schemaName, String tableName);

    protected abstract String getStartIndex(String tableName, String seqField);
    protected abstract String getEndIndex(String tableName, String seqField);

    protected abstract String handleRowId(String sqlQuery);

    protected abstract String handleXdsId(String sqlQuery, Long xdsId);

    protected abstract String getCountSql(String tableName, String schema);
}
