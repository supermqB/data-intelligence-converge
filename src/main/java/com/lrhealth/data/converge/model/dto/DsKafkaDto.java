package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author jinmengyu
 * @date 2024-01-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DsKafkaDto {

    /**
     * tableName_start_position
     * tableName_end_position
     *
     */
    private Map<String, String> paramMap;

    private Long tunnelId;

    private Integer taskId;

}
