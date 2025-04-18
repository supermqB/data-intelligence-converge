package com.lrhealth.data.converge.common.enums;

/**
 * 数据源枚举类
 *
 * @author lr
 */
public enum DataSourceEnum {
    /**
     * 数据源
     */
    ODS("ods"),
    DWD("dwd"),
    SHARE("share");

    private String value;

    DataSourceEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
