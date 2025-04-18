package com.lrhealth.data.converge.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公用异常
 *
 * @author lr
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class CommonException extends RuntimeException {

    private static final long serialVersionUID = 2056523341164047917L;
    private String code = "";
    private String message = "";

    public CommonException(String message) {
        super(message);
        this.code = null;
        this.message = message;
    }


    public CommonException(String code, String message) {
        super(message);
        this.message = message;
        this.code = code;
    }

}
