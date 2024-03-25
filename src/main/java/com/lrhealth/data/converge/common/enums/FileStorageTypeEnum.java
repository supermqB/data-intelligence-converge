package com.lrhealth.data.converge.common.enums;

import java.util.Objects;

/**
 * 文件采集的入库方式
 *
 */
public enum FileStorageTypeEnum {

    /**
     * 1-数据库
     * 2-dicom
     * 3-对象存储
     */
    DATABASE(1),
    DICOM(2),
    OBJECT_STORAGE(3);

    /**
     * 变量分类对应值
     */
    private final Integer value;

    FileStorageTypeEnum(Integer value) {
        this.value = value;
    }

    public static FileStorageTypeEnum of(Integer status) {
        for (FileStorageTypeEnum taskEnum : FileStorageTypeEnum.values()) {
            if (Objects.equals(taskEnum.value, status)) {
                return taskEnum;
            }
        }
        return null;
    }

    public Integer getValue() {
        return value;
    }
}
