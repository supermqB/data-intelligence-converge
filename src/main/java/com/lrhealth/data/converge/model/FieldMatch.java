package com.lrhealth.data.converge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2023-10-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldMatch {

    // 数据格式，如N..3,1
    private String fieldFormat;
    // 数据类型，如N,A
    private String fieldType;
    // 是否有小数
    private boolean hasDecimalPlaces;
}
