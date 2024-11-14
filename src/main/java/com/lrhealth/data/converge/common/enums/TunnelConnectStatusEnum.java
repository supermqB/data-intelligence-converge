package com.lrhealth.data.converge.common.enums;

import com.lrhealth.data.common.exception.CommonException;
import java.util.Objects;

/**
 * @author jinmengyu
 * @date 2023-10-31
 */
public enum TunnelConnectStatusEnum {

    DIRECT(0, "postgresql"),

    FRONT_PROCESSOR(1, "sqlite");

    private Integer code;

    private String value;

    TunnelConnectStatusEnum(Integer code, String value){
        this.code = code;
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }

    public static String getValue(Integer code){
        if (code == null){
            throw new CommonException("状态参数为空");
        }
        for (TunnelConnectStatusEnum statusEnum : TunnelConnectStatusEnum.values()){
            if (Objects.equals(code, statusEnum.code)){
                return statusEnum.value;
            }
        }
        throw new CommonException("未找到匹配的状态值");
    }

}
