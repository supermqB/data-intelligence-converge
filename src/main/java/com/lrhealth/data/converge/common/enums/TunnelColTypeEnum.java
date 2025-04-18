package com.lrhealth.data.converge.common.enums;

/**
 * 采集类型
 *
 * @author lr
 * @since 2022-11-22
 */
public enum TunnelColTypeEnum {

    /**
     * 1-单次自定义采集: 设置采集范围，只执行一次
     * 2-增量采集：设置时间频率，按照表达式增量采集
     * 3-全量采集：设置时间频率，按照表达式全量采集源表
     */
    SINGLE_DEFINED(1),
    FREQUENCY_INCREMENT(2),
    FREQUENCY_TOTAL(3);

    /**
     * 变量分类对应值
     */
    private final Integer value;

    TunnelColTypeEnum(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
