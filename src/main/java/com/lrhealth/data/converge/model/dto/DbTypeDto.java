package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2024-12-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DbTypeDto {

    private String dataType;

    private String typeName;

    private String precision;

}
