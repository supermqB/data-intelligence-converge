package com.lrhealth.data.converge.common.enums;


import com.lrhealth.data.converge.common.exception.CommonException;

import java.util.Optional;

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


    /**
     * 根据编码获取任务状态
     *
     * @param code 编码
     * @return 任务状态
     */
    public static Integer getStatusByCode(Integer code) {
        if (code == null) {
            throw new CommonException("状态参数为空");
        }
        for (XdsStatusEnum statusEnum : XdsStatusEnum.values()) {
            if (code.equals(statusEnum.code)) {
                return statusEnum.code;
            }
        }
        throw new CommonException("未找到匹配的状态值");
    }

    /**
     * 根据编码获取任务状态
     *
     * @param code 编码
     * @return 任务状态
     */
    public static Optional<XdsStatusEnum> getEnumByCode(Integer code) {
        if (code == null) {
            throw new CommonException("状态参数为空");
        }
        for (XdsStatusEnum statusEnum : XdsStatusEnum.values()) {
            if (code.equals(statusEnum.code)) {
                return Optional.of(statusEnum);
            }
        }
        throw new CommonException("未找到匹配的状态值");
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
