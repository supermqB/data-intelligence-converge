package com.lrhealth.data.converge.common.enums;

import java.util.Objects;

/**
 * 增量字段类型
 *
 * @author lr
 * @since 2022-11-22
 */
public enum SeqFieldTypeEnum {

    /**
     *直接调度/重新调度
     */
    SEQ("2")
    , TIME("1");

    /**
     * 变量分类对应值
     */
    private final String value;

    SeqFieldTypeEnum(String value) {
        this.value = value;
    }

    public static SeqFieldTypeEnum of(String status) {
        for (SeqFieldTypeEnum taskEnum : SeqFieldTypeEnum.values()) {
            if (Objects.equals(taskEnum.value, status)) {
                return taskEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }
}
