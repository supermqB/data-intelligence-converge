package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 原始表与原始模型的映射表
 * @author jinmengyu
 * @date 2024-08-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OriginalTableModelDto {

    private Long oriId;

    private String oriName;

    private Long modelId;

    private String modelName;

    private Integer modelDsConfigId;
}
