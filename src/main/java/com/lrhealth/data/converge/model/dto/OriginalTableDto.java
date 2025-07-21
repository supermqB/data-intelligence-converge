package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author jinmengyu
 * @date 2024-01-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OriginalTableDto {

    private String tableName;

    private String tableRemarks;

    private List<ColumnInfoDTO> columnInfoDTOS;

    private Date probeTime;
}
