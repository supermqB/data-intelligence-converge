package com.lrhealth.data.converge.common.enums;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Optional;

/**
 * status enum
 *
 * @author ds
 */
public enum DsStatus {
    /**
     * 状态码
     */
    SUCCESS(0, "success", "成功"),
    /**
     * 状态码
     */
    ERROR(9, "", "");

    private final int code;
    private final String enMsg;
    private final String zhMsg;

    DsStatus(int code, String enMsg, String zhMsg) {
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
    public static Optional<DsStatus> findStatusBy(int code) {
        for (DsStatus status : DsStatus.values()) {
            if (code == status.getCode()) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }
}
