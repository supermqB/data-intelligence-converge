package com.lrhealth.data.converge.common.util;

import com.lrhealth.data.converge.common.enums.DsStatus;

import java.text.MessageFormat;

/**
 * <p>
 * 任务调度-Result
 * </p>
 *
 * @author DS
 * @since 2023/7/21
 */
public class DsResult<T> {
    /**
     * status
     */
    private Integer code;

    /**
     * message
     */
    private String msg;

    /**
     * data
     */
    private T data;

    public DsResult() {
    }

    public DsResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private DsResult(DsStatus status) {
        if (status != null) {
            this.code = status.getCode();
            this.msg = status.getMsg();
        }
    }

    public DsResult(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * Call this function if there is success
     *
     * @param data data
     * @param <T>  type
     * @return resule
     */
    public static <T> DsResult<T> success(T data) {
        return new DsResult<>(DsStatus.SUCCESS.getCode(), DsStatus.SUCCESS.getMsg(), data);
    }

    public static DsResult success() {
        return success(null);
    }

    public boolean isSuccess() {
        return this.isStatus(DsStatus.SUCCESS);
    }

    public boolean isFailed() {
        return !this.isSuccess();
    }

    public boolean isStatus(DsStatus status) {
        return this.code != null && this.code.equals(status.getCode());
    }

    /**
     * Call this function if there is any error
     *
     * @param status status
     * @return result
     */
    public static <T> DsResult<T> error(DsStatus status) {
        return new DsResult<>(status);
    }

    /**
     * Call this function if there is any error
     *
     * @param status status
     * @param args   args
     * @return result
     */
    public static <T> DsResult<T> errorWithArgs(DsStatus status, Object... args) {
        return new DsResult<>(status.getCode(), MessageFormat.format(status.getMsg(), args));
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Status{"
                + "code='" + code
                + '\'' + ", msg='"
                + msg + '\''
                + ", data=" + data
                + '}';
    }

    public Boolean checkResult() {
        return this.code == DsStatus.SUCCESS.getCode();
    }
}

