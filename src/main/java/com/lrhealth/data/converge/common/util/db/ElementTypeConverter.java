package com.lrhealth.data.converge.common.util.db;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ElementTypeConverter {
    private static Map<String, ElementTypeConverter> converters = new HashMap<>();
    static {
        converters.put("hive", new HiveType());
        converters.put("default", new HiveType());
        // @TODO, add other platform support.
    }

    public static ElementTypeConverter type(String dbType) {
        if (dbType == null) {
            log.error(
                    "Please provide proper db type while retrieving a DB type converter.default Hive converter is used this time.");
            dbType = "default";
        }
        return converters.get(dbType.toLowerCase());
    }

    private String INT = "INT";
    private String BIGINT = "BIGINT";
    private String DOUBLE = "DOUBLE";
    private String TIMESTAMP = "TIMESTAMP";
    private String DATE = "DATE";
    private String STRING = "STRING";
    private String BOOLEAN = "BOOLEAN";

    public String process(String format, String elemType) {
        if (isBoolean(elemType)) {
            return this.BOOLEAN;
        }

        if (isFLoat(format)) {
            return this.DOUBLE;
        }

        if (isBigInt(format)) {
            return this.BIGINT;
        }

        if (isInt(format)) {
            return this.INT;
        }

        if (isDateTime(format)) {
            return this.TIMESTAMP;
        }

        if (isDate(format)) {
            return this.DATE;
        }

        return this.STRING;
    }

    protected boolean isBigInt(String format) {
        boolean isN = format.startsWith("N");
        if (!isN)
            return false;

        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(format);
        if (matcher.find()) {
            String numPart = matcher.group();
            return Integer.parseInt(numPart) > 9;
        } else {
            return false;
        }
    }

    protected boolean isInt(String format) {
        boolean isN = format.startsWith("N");
        if (!isN)
            return false;

        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(format);
        if (matcher.find()) {
            String numPart = matcher.group();
            return Integer.parseInt(numPart) <= 9;
        } else {
            return true;
        }
    }

    protected boolean isFLoat(String format) {
        // 同时满足：以 'N' 开头，且包含逗号
        return format.startsWith("N") && format.contains(",");
    }

    protected boolean isBoolean(String elemType) {
        return "L".equals(elemType);
    }

    protected boolean isDateTime(String format) {
        return format.startsWith("DT");
    }

    protected boolean isDate(String format) {
        return format.startsWith("D") && !format.startsWith("DT");
    }

}
