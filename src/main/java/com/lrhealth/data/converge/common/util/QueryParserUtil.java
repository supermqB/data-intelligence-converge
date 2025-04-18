package com.lrhealth.data.converge.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jinmengyu
 * @date 2023-09-15
 */
public class QueryParserUtil {

    private QueryParserUtil(){}

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
