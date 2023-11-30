package com.lrhealth.data.converge.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yuanbaiyu
 * @since 2023/9/21 13:23
 */
@Getter
@AllArgsConstructor
public enum LibraryTableModelEnum {

    DATABASE_TO_FILE(0, "库到文件模式"),

    DATABASE_TO_DATABASE(1, "库到库模式");


    private final Integer code;
    private final String description;

    public static LibraryTableModelEnum of(Integer code) {
        for (LibraryTableModelEnum taskEnum : LibraryTableModelEnum.values()) {
            if (taskEnum.code.equals(code)) {
                return taskEnum;
            }
        }
        return null;
    }

}
