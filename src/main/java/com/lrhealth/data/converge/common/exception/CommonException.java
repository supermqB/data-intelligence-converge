package com.lrhealth.data.converge.common.exception;

import com.lrhealth.data.converge.common.enums.MsgEnum;
import lombok.Getter;

/**
 * 公用异常
 *
 * @author lr
 **/
@Getter
public class CommonException extends RuntimeException {
    private String code = "";
    private String message = "";

    public CommonException(String message) {
        super(message);
        this.code = null;
        this.message = message;
    }

    public CommonException(MsgEnum msgEnum) {
        super(msgEnum.getMessage());
        this.message = msgEnum.getMessage();
        this.code = msgEnum.getCode();
    }

    public CommonException(String code, String message) {
        super(message);
        this.message = message;
        this.code = code;
    }

    public CommonException(String message, Throwable e) {
        super(message, e);
        this.message = String.format("%s %s", message, e.getMessage());
    }
}
