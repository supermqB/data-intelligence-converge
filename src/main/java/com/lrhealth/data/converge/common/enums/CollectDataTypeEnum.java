package com.lrhealth.data.converge.common.enums;

/**
 * 数据汇聚-数据类型
 *
 * @author lr
 */
public enum CollectDataTypeEnum {

    /**
     * 数据类别：1-字典数据，2-基础数据，3-报告文书，4-影像文件，5-业务数据
     */
    DICT("1", "字典数据"), BASE("2", "基础数据"), REPORT("3", "报告文书"), IMAGE("4", "影像文件"), BUSINESS("5", "业务数据");

    private final String code;

    private final String info;

    /**
     * 是否是基础数据
     *
     * @param code 数据分类
     * @return 是-true
     */
    public static boolean isBaseData(String code) {
        return BASE.code.equals(code);
    }

    /**
     * 是否是字典数据
     *
     * @param code 数据分类
     * @return 是-true
     */
    public static boolean isDictData(String code) {
        return DICT.code.equals(code);
    }

    /**
     * 是否是基础数据或字典数据
     *
     * @param code 数据分类
     * @return 是-true
     */
    public static boolean isBaseOrDictData(String code) {
        return isDictData(code) || isBaseData(code);
    }

    /**
     * 是否是业务数据
     *
     * @param code 数据分类
     * @return 是-true
     */
    public static boolean isBusinessData(String code) {
        return BUSINESS.code.equals(code);
    }

    CollectDataTypeEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

    public static String getCodeByInfo(String info) {
        for (CollectDataTypeEnum dataType : CollectDataTypeEnum.values()) {
            if (dataType.getInfo().equals(info)) {
                return dataType.getCode();
            }
        }
        return null;
    }
}
