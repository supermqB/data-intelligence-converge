package com.lrhealth.data.converge.common.enums;

import com.lrhealth.data.common.exception.CommonException;

/**
 * @author jinmengyu
 * @date 2023-08-08
 */
public enum ConvergeType {

    DATA_BASE(0, "db"),

    FILE(1, "file");

    private int code;

    private String type;

    ConvergeType(int code, String type){
        this.code = code;
        this.type = type;
    }

    public Integer getCode(){return this.code;}

    public String getType(){return  this.type;}

    public static String getConvergeType(Integer code) {
        if (code == null) {
            throw new CommonException("状态参数为空");
        }
        for (ConvergeType converge : ConvergeType.values()) {
            if (code.equals(converge.code)) {
                return converge.type;
            }
        }
        throw new CommonException("未找到匹配的状态值");
    }
}
