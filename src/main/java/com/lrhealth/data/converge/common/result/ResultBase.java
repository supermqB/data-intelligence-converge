package com.lrhealth.data.converge.common.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lrhealth.data.converge.common.enums.ApiMsgEnum;
import com.lrhealth.data.converge.common.enums.MsgEnum;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>返回结果封装类 </p>
 *
 * @author lr
 */
@Getter
@Setter
public class ResultBase<T> implements Serializable {
    /**
     * 接口状态
     */
    private boolean success = false;

    /**
     * 消息文本
     */
    private String message = "";

    /**
     * 消息码
     */
    private String code = "";
    /**
     * 分页的页数
     */
    private Integer pageNum = null;
    /**
     * 分页的每一页大小
     */
    private Integer pageSize = null;

    /**
     * 消息枚举
     */
    @JsonIgnore
    private MsgEnum msgEnum;

    /**
     * 返回结果值 泛型
     */
    private T value;

    public ResultBase() {
        super();
    }

    /**
     * 导入枚举构造方法-失败
     *
     * @param msgEnum 枚举类
     */
    public static ResultBase<String> fail(MsgEnum msgEnum) {
        return fail(msgEnum.getCode(), msgEnum.getMessage(), null);
    }

    public ResultBase(boolean success, String errorCode, String message) {
        super();
        this.success = success;
        this.message = message;
        this.code = errorCode;
    }

    /**
     * 构造器-成功
     */
    public static ResultBase<Void> success() {
        return success(null);
    }

    public static <T> ResultBase<T> success(Integer pageNum, Integer pageSize, T value) {
        ResultBase<T> resultBase = success(value);
        resultBase.setPageNum(pageNum);
        resultBase.setPageSize(pageSize);
        return resultBase;
    }

    public static <T> ResultBase<T> success(T value) {
        ResultBase<T> resultBase = new ResultBase<>(true, ApiMsgEnum.SUCCESS.getCode(), ApiMsgEnum.SUCCESS.getMessage());
        resultBase.setValue(value);
        return resultBase;
    }

    public static <T> ResultBase<T> fail() {
        return fail(ApiMsgEnum.FAIL.getMessage());
    }

    public static <T> ResultBase<T> fail(String message) {
        return fail(ApiMsgEnum.FAIL.getCode(), message);
    }

    public static <T> ResultBase<T> fail(String errorCode, String message) {
        return fail(errorCode, message, null);
    }

    public static <T> ResultBase<T> fail(String errorCode, String message, T value) {
        ResultBase<T> resultBase = new ResultBase<>(false, errorCode, message);
        resultBase.setValue(value);
        return resultBase;
    }

    public static <T> ResultBase<T> fail(String message, T value) {
        ResultBase<T> resultBase = new ResultBase<>(false, ApiMsgEnum.FAIL.getCode(), message);
        resultBase.setValue(value);
        return resultBase;
    }

    @Override
    public String toString() {
        return "ResultBase [isSuccess=" + success + ", message=" + message + ", getCode=" + code + ", value=" + value + "]";
    }
}
