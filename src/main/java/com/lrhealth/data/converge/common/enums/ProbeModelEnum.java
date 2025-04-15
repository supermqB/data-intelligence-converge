package com.lrhealth.data.converge.common.enums;

/**
 * @author jinmengyu
 * @date 2025-04-15
 */
public enum ProbeModelEnum {

    // 字段空值率
    COLUMN_NULLABLE(1),
    // 字段值域频率
    COLUMN_VALUEFREQ(2);

    private Integer code;

    ProbeModelEnum(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return code;
    }
}
