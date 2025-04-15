package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author jinmengyu
 * @date 2025-04-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OriDataProbeDTO {

    private String sysCode;

    private Integer dsConfId;

    private String tableName;

    private Date probeEndTime;

    private Long dataCount;

    private Long dataSize;

    private String oriDataList;

    private List<ColumnNullableDTO> nullableList;
}
