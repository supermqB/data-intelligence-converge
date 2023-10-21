package com.lrhealth.data.converge.common.enums;

import java.util.Objects;

/**
 * 治理任务状态
 *
 * @author lr
 * @since 2022-11-22
 */
public enum ExecStatusEnum {

    /**
     *直接调度/重新调度
     */
    DIRECT(0), REFRESH(1);

    /**
     * 变量分类对应值
     */
    private final Integer value;

    ExecStatusEnum(Integer value) {
        this.value = value;
    }

    public static ExecStatusEnum of(Integer status) {
        for (ExecStatusEnum taskEnum : ExecStatusEnum.values()) {
            if (Objects.equals(taskEnum.value, status)) {
                return taskEnum;
            }
        }
        return null;
    }

    public Integer getValue() {
        return value;
    }
}
