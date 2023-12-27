package com.lrhealth.data.converge.common.exception;

/**
 * 自定义异常
 *
 * @author lr-app
 */
public class FileStatusException extends RuntimeException {
    private static final long serialVersionUID = 1L;


    private String message;

    public FileStatusException(String message) {
        this.message = message;
    }

    public FileStatusException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
