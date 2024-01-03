package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2024-01-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnInfoDTO {

    /**
     * 字段名称
     */
    private String columnName;

    /**
     * 字段类型
     */
    private String columnTypeName;

    /**
     * 注释
     */
    private String remark;

    /**
     * 字段长度
     */
    private Integer columnLength;

    /**
     * 默认值
     */
    private String columnDef;

    /**
     * 是否必填
     * 0-必填 1-非必填
     */
    private Integer nullable;

    /**
     * 是否主键
     * 0-非主键 1-主键
     */
    private Integer primaryKeyFlag;

    /**
     * 主键序列号
     */
    private Integer primaryKeySeq;

}
