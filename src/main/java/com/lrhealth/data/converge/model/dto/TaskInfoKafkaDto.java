package com.lrhealth.data.converge.model.dto;

import lombok.*;

/**
 * @author jinmengyu
 * @date 2024-02-20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TaskInfoKafkaDto extends TunnelStatusKafkaDto{

    private Integer taskId;

    private String startTime;

    private String endTime;

    private Integer status;
}
