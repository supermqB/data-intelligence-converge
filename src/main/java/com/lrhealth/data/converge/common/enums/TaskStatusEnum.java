package com.lrhealth.data.converge.common.enums;

import java.util.Objects;

/**
 * 治理任务状态
 *
 * @author lr
 * @since 2022-11-22
 */
public enum TaskStatusEnum {

    /**
     * preparing/extracting/dumping/waiting_transfer/downloaded/done/failed
     */
    PREPARING(0), EXTRACTING(1), DUMPING(2),  WAITING_TRANSFER(3),
    DOWNLOADED(4), FAILED(6), DONE(5), STREAMING(7);

    /**
     * 变量分类对应值
     */
    private final Integer value;

    TaskStatusEnum(Integer value) {
        this.value = value;
    }

    public static TaskStatusEnum of(Integer status) {
        for (TaskStatusEnum taskEnum : TaskStatusEnum.values()) {
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
