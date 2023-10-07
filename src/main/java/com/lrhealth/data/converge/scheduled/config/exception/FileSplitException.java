package com.lrhealth.data.converge.scheduled.config.exception;

/**
 * 自定义异常
 *
 * @author lr-app
 */
public class FileSplitException extends RuntimeException {
    private static final long serialVersionUID = 1L;


    private String message;

    public FileSplitException(String message) {
        this.message = message;
    }

    public FileSplitException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
