package com.lrhealth.data.converge.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yuanbaiyu
 * @since 2023/9/21 13:23
 */
@Getter
@AllArgsConstructor
public enum TunnelCMEnum {

    LIBRARY_TABLE("1", "库表模式"),

    CDC_LOG("2", "日志模式（CDC）"),

    FILE_MODE("3", "文件模式"),

    INTERFACE_MODE("4", "接口模式"),

    QUEUE_MODE("5", "队列模式");

    private final String code;
    private final String description;

    public static TunnelCMEnum of(String code) {
        for (TunnelCMEnum taskEnum : TunnelCMEnum.values()) {
            if (taskEnum.code.equals(code)) {
                return taskEnum;
            }
        }
        return null;
    }

}
