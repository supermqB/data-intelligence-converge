package com.lrhealth.data.converge.scheduled.config.exception;

/**
 * 自定义异常
 *
 * @author lr-app
 */
public class FileDeleteException extends RuntimeException {
    private static final long serialVersionUID = 1L;


    private String message;

    public FileDeleteException(String message) {
        this.message = message;
    }

    public FileDeleteException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
