package com.lrhealth.data.converge.common.enums;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 接口的消息码枚举
 *
 * @author lr
 */
public enum ApiMsgEnum implements MsgEnum {
    /**
     * 操作成功
     */
    SUCCESS(Boolean.TRUE, "200", "成功"),
    /**
     * 操作失败
     */
    FAIL(Boolean.FALSE, "-1", "失败"),
    ;

    public Boolean success;
    private String code;
    private String message;

    ApiMsgEnum(Boolean success, String code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String errorMsg) {
        this.message = message;
    }

    public static Map<String, String> getAll() {
        Map<String, String> retMap = new LinkedHashMap<String, String>();
        ApiMsgEnum[] enumArr = ApiMsgEnum.values();
        for (ApiMsgEnum aEnum : enumArr) {
            retMap.put(aEnum.getCode(), aEnum.getMessage());
        }
        return retMap;
    }
}
