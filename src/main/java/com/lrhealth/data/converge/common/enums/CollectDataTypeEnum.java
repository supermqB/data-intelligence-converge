package com.lrhealth.data.converge.common.enums;

/**
 * 数据汇聚-数据类型
 *
 * @author lr
 */
public enum CollectDataTypeEnum {

    /**
     * 数据类别：1-字典数据，2-基础数据，3-报告文书，4-影像文件，5-业务数据
     */
    DICT("1", "字典数据"), BASE("2", "基础数据"), REPORT("3", "报告文书"), IMAGE("4", "影像文件"), BUSINESS("5", "业务数据");

    private final String code;

    private final String info;

    CollectDataTypeEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

}
