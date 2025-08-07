package com.lrhealth.data.converge.common.enums;

/**
 * 消息枚举抽象类
 *
 * @param <E>
 * @author admin
 */
public interface MsgEnum {
    String getCode();

    void setCode(String code);

    String getMessage();

    void setMessage(String message);

    Boolean getSuccess();

    void setSuccess(Boolean success);
}
