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
    INIT(0),
    /**
     * 已完成
     */
    COMPLETED(1);

    /**
     * 状态编码
     */
    private final Integer code;


    /**
     * 构建任务状态枚举类
     *
     * @param code 编码
     */
    XdsStatusEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

}
