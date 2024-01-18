package com.lrhealth.data.converge.ds.dto;



import com.lrhealth.data.converge.ds.enums.DsStatusEnum;

import java.text.MessageFormat;

/**
 * 调用DS的结果类
 * @param <T>
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

    private boolean failed;
    private boolean success;

    public DsResult() {
    }

    public DsResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public DsResult(DsStatusEnum dsStatusEnum) {
        if (dsStatusEnum != null) {
            this.code = dsStatusEnum.getCode();
            this.msg = dsStatusEnum.getMsg();
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
     * @param <T> type
     * @return resule
     */
    public static <T> DsResult<T> success(T data) {
        return new DsResult<>(DsStatusEnum.SUCCESS.getCode(), DsStatusEnum.SUCCESS.getMsg(), data);
    }

    public static <T> DsResult<T> success() {
        return success(null);
    }

    public boolean isSuccess() {
        return this.isStatus(DsStatusEnum.SUCCESS);
    }

    public boolean isFailed() {
        return !this.isSuccess();
    }

    public boolean isStatus(DsStatusEnum dsStatusEnum) {
        return this.code != null && this.code.equals(dsStatusEnum.getCode());
    }

    /**
     * Call this function if there is any error
     *
     * @param dsStatusEnum status
     * @return result
     */
    public static <T> DsResult<T> error(DsStatusEnum dsStatusEnum) {
        return new DsResult<>(dsStatusEnum);
    }

    /**
     * Call this function if there is any error
     *
     * @param dsStatusEnum status
     * @param args args
     * @return result
     */
    public static <T> DsResult<T> errorWithArgs(DsStatusEnum dsStatusEnum, Object... args) {
        return new DsResult<>(dsStatusEnum.getCode(), MessageFormat.format(dsStatusEnum.getMsg(), args));
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
        return this.code == DsStatusEnum.SUCCESS.getCode();
    }
}
