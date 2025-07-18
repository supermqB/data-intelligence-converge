package com.lrhealth.data.converge.common.util.db;

import lombok.Data;

@Data
public class FieldInfo {
    private String columnName;
    private String fieldType;
    private boolean primaryKey;
    private boolean autoIncrement;
}
