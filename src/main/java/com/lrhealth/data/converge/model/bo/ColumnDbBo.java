package com.lrhealth.data.converge.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2023-10-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnDbBo {

    private String columnName;

    private String fieldType;

    private Integer fieldLength;

    private String seqFlag;
}
