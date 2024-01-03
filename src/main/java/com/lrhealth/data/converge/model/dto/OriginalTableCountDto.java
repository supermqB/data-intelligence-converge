package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author jinmengyu
 * @date 2024-01-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OriginalTableCountDto {

    private String sysCode;

    private Map<String, Long> tableCountMap;
}
