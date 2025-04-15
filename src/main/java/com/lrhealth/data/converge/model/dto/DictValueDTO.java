package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2025-04-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictValueDTO {

    private String value;

    private String frequency;
}
