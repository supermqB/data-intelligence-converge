package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2023-12-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FepScheduledDto {

    private Long tunnelId;

    private Integer taskId;
}
