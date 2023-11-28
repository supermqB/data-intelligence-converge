package com.lrhealth.data.converge.common.util;

import com.lrhealth.data.converge.common.enums.ResStatus;
import lombok.Data;

/**
 * <p>
 * Result
 * </p>
 *
 * @author DS
 * @since 2023/7/21
 */
@Data
public class ResResult<T> {
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

    private ResResult() {
    }

    public ResResult(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * Call this function if there is success
     *
     * @param data data
     * @param <T>  type
     * @return result
     */
    public static <T> ResResult<T> success(T data) {
        return new ResResult<>(ResStatus.SUCCESS.getCode(), ResStatus.SUCCESS.getMsg(), data);
    }

    public static ResResult<Void> success() {
        return success(null);
    }

    public boolean isSuccess() {
        return this.isStatus(ResStatus.SUCCESS);
    }

    public boolean isFailed() {
        return !this.isSuccess();
    }

    public boolean isStatus(ResStatus status) {
        return this.code != null && this.code.equals(status.getCode());
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
}

