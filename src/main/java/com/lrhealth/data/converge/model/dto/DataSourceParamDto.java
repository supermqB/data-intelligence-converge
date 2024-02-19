package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2024-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceParamDto {

    private String orgCode;

    private String sysCode;

    private Integer dsConfigId;

    /**
     *  structure 1-库表 2-视图
     */
    private Integer structure;
}
