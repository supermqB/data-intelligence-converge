package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 增量配置
 * @author jinmengyu
 * @date 2025-04-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncrConfigDTO {

    private String seqField;

    private String seqFieldType;

    private String incrTime;

    private String startIndex;

    private String endIndex;

    private String maxSql;
}
