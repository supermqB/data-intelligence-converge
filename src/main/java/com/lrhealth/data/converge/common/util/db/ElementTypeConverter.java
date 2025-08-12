package com.lrhealth.data.converge.common.util.db;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ElementTypeConverter {

    protected String INT = "INT";
    protected String BIGINT = "BIGINT";
    protected String DOUBLE = "DOUBLE";
    protected String TIMESTAMP = "TIMESTAMP";
    protected String DATE = "DATE";
    protected String STRING = "STRING";

    public String process(String format) {
        if (isFLoat(format)) {
            return DOUBLE;
        }

        if (isBigInt(format)) {
            return BIGINT;
        }

        if (isInt(format)) {
            return INT;
        }

        if (isDateTime(format)) {
            return TIMESTAMP;
        }

        if (isDate(format)) {
            return DATE;
        }

        return STRING;
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
            return Integer.parseInt(numPart) < 9;
        } else {
            return true;
        }
    }

    protected boolean isFLoat(String format) {
        // 同时满足：以 'N' 开头，且包含逗号
        return format.startsWith("N") && format.contains(",");
    }

    protected boolean isDateTime(String format) {
        return format.startsWith("DT");
    }

    protected boolean isDate(String format) {
        return format.startsWith("D") && !format.startsWith("DT");
    }

}
