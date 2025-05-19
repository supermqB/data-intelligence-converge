package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2025-05-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncrColumnDTO {

    private String tableName;

    private String columnName;

    /**
     * 增量类型
     * 1-数字 2-时间
     */
    private Integer incrType;

    private String timeStartPoint;

    private String timeEndPoint;

    private String seqStartPoint;
}
