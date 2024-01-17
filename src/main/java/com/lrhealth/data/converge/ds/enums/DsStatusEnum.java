package com.lrhealth.data.converge.ds.enums;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Optional;

/**
 * DS返回状态的枚举
 */
public enum DsStatusEnum {

    SUCCESS(0, "success", "成功");

    private final int code;
    private final String enMsg;
    private final String zhMsg;

    DsStatusEnum(int code, String enMsg, String zhMsg) {
        this.code = code;
        this.enMsg = enMsg;
        this.zhMsg = zhMsg;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
            return this.zhMsg;
        } else {
            return this.enMsg;
        }
    }

    /**
     * Retrieve Status enum entity by status code.
     */
    public static Optional<DsStatusEnum> findStatusBy(int code) {
        for (DsStatusEnum dsStatusEnum : DsStatusEnum.values()) {
            if (code == dsStatusEnum.getCode()) {
                return Optional.of(dsStatusEnum);
            }
        }
        return Optional.empty();
    }
}
