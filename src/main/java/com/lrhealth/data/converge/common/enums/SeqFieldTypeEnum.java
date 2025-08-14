package com.lrhealth.data.converge.common.enums;

/**
 * 增量字段类型
 *
 * @author lr
 * @since 2022-11-22
 */
public enum SeqFieldTypeEnum {

    /**
     * 数字/时间序列
     */
    SEQ("1"), TIME("2");

    /**
     * 变量分类对应值
     */
    private final String value;

    SeqFieldTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
