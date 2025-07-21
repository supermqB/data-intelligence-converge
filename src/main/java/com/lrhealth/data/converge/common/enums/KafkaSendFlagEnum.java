package com.lrhealth.data.converge.common.enums;

/**
 * <p>
 * 发送kafka消息结果：0-未发送;1-已发送
 * </p>
 *
 * @author lr
 * @since 2023/7/21
 */
public enum KafkaSendFlagEnum {


    /**
     * 发送kafka消息结果：0-未发送;1-已发送
     */
    NONE(0, "未发送"),
    SENT(1, "已发送");

    private final Integer code;

    private final String info;

    /**
     * 是否已发送
     *
     * @param code 编码
     * @return 是-true
     */
    public static boolean isSent(Integer code) {
        return SENT.code.equals(code);
    }

    KafkaSendFlagEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }

    public Integer getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }
}
