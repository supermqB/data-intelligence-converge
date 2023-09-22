package com.lrhealth.data.converge.scheduled.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2023-09-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskLogDto {

    private Integer logId;

    private String logDetail;

    private String logTime;

}
