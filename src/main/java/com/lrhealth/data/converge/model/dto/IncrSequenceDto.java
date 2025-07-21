package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2024-02-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncrSequenceDto {

    private Long tunnelId;

    private String tableName;

    private String seqField;

    /**
     * 增量最新序列
     */
    private String incrSequence;
}
