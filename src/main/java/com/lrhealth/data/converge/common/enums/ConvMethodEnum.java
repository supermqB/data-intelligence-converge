package com.lrhealth.data.converge.common.enums;

import lombok.Getter;

/**
 * <p>
 * 汇聚方式：1-库表模式，2-日志模式（CDC），3-文件模式，4-接口模式，5-队列模式
 * </p>
 *
 * @author lr
 * @since 2023/7/21
 */
@Getter
public enum ConvMethodEnum {
    /**
     * 汇聚方式：1-库表模式，2-日志模式（CDC），3-文件模式，4-接口模式，5-队列模式
     */
    LOG("2", "日志模式（CDC）"),
    DB_TABLE("1", "库表模式"),
    FILE("3", "文件模式"),
    INTERFACE("4", "接口模式"),
    QUEUE("5", "队列模式"),
    ;

    private final String code;

    private final String info;

    ConvMethodEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }
}
