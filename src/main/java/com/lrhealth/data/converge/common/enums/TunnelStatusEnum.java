package com.lrhealth.data.converge.common.enums;

import java.util.Objects;

/**
 * 治理任务状态
 *
 * @author lr
 * @since 2022-11-22
 */
public enum TunnelStatusEnum {

    /**
     * 管道状态：0-待配置，1-任务已排班，2-任务执行中，3-暂停，4-废弃
     */
    INIT(0), SCHEDULING(1), PROCESSING(2), PAUSE(3),  ABANDON(4);

    /**
     * 变量分类对应值
     */
    private final Integer value;

    TunnelStatusEnum(Integer value) {
        this.value = value;
    }

    public static TunnelStatusEnum of(Integer status) {
        for (TunnelStatusEnum taskEnum : TunnelStatusEnum.values()) {
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
