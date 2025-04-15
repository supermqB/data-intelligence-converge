package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2025-04-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnNullableDTO {

    private String columnName;

    private Double nullable;
}
