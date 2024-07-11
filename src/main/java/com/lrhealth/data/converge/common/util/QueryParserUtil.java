package com.lrhealth.data.converge.common.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReUtil;
import com.lrhealth.data.converge.model.vo.DbValidVo;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author jinmengyu
 * @date 2023-09-15
 */
public class QueryParserUtil {

    private QueryParserUtil(){}

    private static final String LOW_QUERY = "select(.*)from";
    private static final String UP_QUERY = "SELECT(.*)FROM";
    private static final String SQL_QUERY_TEMPLATE = "select(.*)from";

    private static final String TEMPLATE_QUERY = "select\\s+(\\w+\\.\\*)";

    public static List<String> queryColumnParser(String sqlQuery){
//        String fieldPlaceholder = ReUtil.get(SQL_QUERY_TEMPLATE, "\\{([^}]+)}", 1);
//        String fields = sqlQuery.replace(fieldPlaceholder, "").trim();
//        return Arrays.asList(fields.split(","));
        String s = ReUtil.get(SQL_QUERY_TEMPLATE, sqlQuery, 1);
        String[] split = s.trim().split(",");
        return Arrays.stream(split).map(column -> {
            return ReUtil.get(".*\\.(.*)", column, 1);
        }).collect(Collectors.toList());
    }

    public static String getSqlColumn(String sqlQuery){
        String column = ReUtil.get(LOW_QUERY, sqlQuery, 1);
        if (CharSequenceUtil.isBlank(column)){
            return ReUtil.get(UP_QUERY, sqlQuery, 1);
        }
        return column;
    }

    public static String getTemplateQuery(String sqlQuery){
        String column = ReUtil.get(TEMPLATE_QUERY, sqlQuery, 1);
        if (CharSequenceUtil.isBlank(column)){
            return ReUtil.get(TEMPLATE_QUERY, sqlQuery, 1);
        }
        return column;
    }

    //replace
    public static String getDbType(String url) {
        if (url.startsWith("hdfs://")){
            return "hdfs";
        }
        Pattern p = Pattern.compile("jdbc:(?<db>\\w+):.*((//)|@)(?<host>.+):(?<port>\\d+)?(?:/(?<dbName>[^?;]*))?.*$");
        Matcher m = p.matcher(url);
        String db = null;
        if(m.find()) {
            db = m.group("db");
            String host = m.group("host");
            String port = m.group("port");
            String dbName = m.group("dbName");
        }
        return db;
    }

    public static void main(String[] args) {
        String url = "jdbc:oceanbase://172.16.29.68:2883/";
        String dbType = getDbType(url);
        System.out.println(dbType);
    }

    public static DbValidVo getDbMessage(String url) {
        if (CharSequenceUtil.isBlank(url)) return null;
        Pattern p = Pattern.compile("jdbc:(?<db>\\w+):.*((//)|@)(?<host>.+):(?<port>\\d+)(/|(;databaseName=)|:)(?<dbName>\\w+)\\??.*");
        Matcher m = p.matcher(url);
        DbValidVo dbValidVo = new DbValidVo();
        if(m.find()) {
            dbValidVo.setJdbcUrl(url);
            dbValidVo.setDbType(m.group("db"));
            dbValidVo.setHost(m.group("host"));
            dbValidVo.setPort(m.group("port"));
        }
        return dbValidVo;
    }
    //replace
    public static String getDbSchema(String url) {
        Pattern p = Pattern.compile("jdbc:(?<db>\\w+):.*((//)|@)(?<host>.+):(?<port>\\d+)(/|(;databaseName=)|:)(?<dbName>\\w+)\\??.*");
        Matcher m = p.matcher(url);
        String dbName = null;
        if(m.find()) {
            dbName = m.group("dbName");
        }
        return dbName;
    }

}
