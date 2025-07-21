package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jinmengyu
 * @date 2024-01-03
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OriginalStructureDto {

    private Integer dsConfId;

    private String orgCode;

    private String sysCode;

    private List<OriginalTableDto> originalTables;
}
