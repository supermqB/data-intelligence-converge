package com.lrhealth.data.converge.common.enums;


/**
 * Xds状态
 * 0 初始化，1 汇聚完成，2 汇聚失败
 *
 * @author lr
 * @since 2022/10/16 12:36
 */
public enum XdsStatusEnum {
    /**
     * 初始化
     */
    INIT(0, "初始化"),
    /**
     * 已完成
     */
    COMPLETED(1, "已完成"),

    /**
     * 失败
     */
    FAILURE(2, "治失败");

    /**
     * 状态编码
     */
    private final Integer code;
    /**
     * 状态描述
     */
    private final String desc;

    /**
     * 构建任务状态枚举类
     *
     * @param code 编码
     * @param desc 描述
     */
    XdsStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

}
