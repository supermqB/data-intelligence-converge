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
public class ResultRecordDto {

    private Integer taskId;

    private String tableName;

    private Integer count;
}
