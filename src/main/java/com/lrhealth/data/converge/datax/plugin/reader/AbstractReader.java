package com.lrhealth.data.converge.datax.plugin.reader;

import cn.hutool.log.Log;
import com.lrhealth.data.converge.common.enums.DataXPluginEnum;
import com.lrhealth.data.converge.model.DataBaseMessageDTO;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jinmengyu
 * @date 2023-08-31
 */
public abstract class AbstractReader {
    private static final Log log = Log.get(AbstractReader.class);
    protected AbstractReader(){
    }

    public List<String> generateDatabaseReader(DataBaseMessageDTO dto, String oriFilePath){
        long stepStart = System.currentTimeMillis();
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        String url = getJdbcUrl(dto.getHost(), dto.getPort(), dto.getDatabaseName());
        dto.setJdbcUrl(url);
        try {
            Class.forName(getDataBase());
            con= DriverManager.getConnection(url, dto.getUserName(), dto.getUserPassword());
            st=con.createStatement();
            rs=st.executeQuery(tableSql(dto.getSchemaName()));
            List<String> tableList = new ArrayList<>();
            while(rs.next()) {
                String tableName = rs.getString(1);
                tableList.add(tableName);
            }
            log.info("tableNumber: {}, name: {}", tableList.size(), tableList);
            for (String table : tableList){
                List<String> columnList = new ArrayList<>();
                rs=st.executeQuery(columnSql(dto.getSchemaName(), table));
                while(rs.next()) {
                    String tableName = rs.getString(1);
                    columnList.add(tableName);
                }
                log.info("table: {}, columnSize: {}, name: {}",table, columnList.size(), columnList);
                readerJson(columnList, table, dto, oriFilePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(rs!=null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(st!=null) {
                try {
                    st.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(con!=null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        File[] fileList = new File(dto.getJsonSavePath()).listFiles();
        return Arrays.stream(fileList).filter(File::isFile).filter(file -> file.lastModified() >= stepStart)
                .map(File::getName).collect(Collectors.toList());
    }

    private void readerJson(List<String> columnList, String table, DataBaseMessageDTO dto, String oriFilePath){
        try {
            InputStream is = AbstractReader.class.getClassLoader().getResourceAsStream("dataX/csv.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            // tmpfile为缓存文件，代码运行完毕后此文件将重命名为源文件名字。
            File tmpfile = new File(dto.getJsonSavePath() + "/" + table + ".json");
            // 内存流, 作为临时流
            CharArrayWriter tempStream = new CharArrayWriter();
            String str = null;
            while ((str = reader.readLine()) != null) {// 替换每行中, 符合条件的字符串
                if (str.contains("readerPlugin-")) {
                    str = str.replace("readerPlugin-", DataXPluginEnum.getDatabasePlugin(dto.getDatabase()));
                }else if (str.contains("readerTableName")) {
                    str = str.replace("readerTableName", table);
                }else if (str.contains("csvHeader")) {
                    String collect = columnList.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(","));
                    str = str.replace("csvHeader", collect);
                }else if (str.contains("readerUsername")){
                    str = str.replace("readerUsername", dto.getUserName());
                }else if (str.contains("readerPassword")){
                    str = str.replace("readerPassword", dto.getUserPassword());
                }else if (str.contains("readerJdbcUrl")){
                    str = str.replace("readerJdbcUrl", dto.getJdbcUrl());
                }else if (str.contains("fileSavePath")){
                    str = str.replace("fileSavePath", oriFilePath);
                }else if (str.contains("readerQuerySql")){
                    str = str.replace("readerQuerySql", readerQuerySql(dto.getSchemaName(), table, dto.getCondition()));
                }
                // 将该行写入内存
                tempStream.write(str);
                // 添加换行符
                tempStream.append(System.getProperty("line.separator"));
            }
            is.close();
            // 将内存中的流 写入 文件
            FileWriter out = new FileWriter(tmpfile);
            tempStream.writeTo(out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract String getDataBase();

    protected abstract String getJdbcUrl(String host, String port, String databaseName);

    protected abstract String tableSql(String schemaName);

    protected abstract String columnSql(String schemaName, String tableName);

    protected abstract String readerQuerySql(String schemaName, String tableName, String condition);




}
