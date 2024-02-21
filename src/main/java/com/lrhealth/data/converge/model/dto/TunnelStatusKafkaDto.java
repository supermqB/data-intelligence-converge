package com.lrhealth.data.converge.model.dto;

import lombok.Data;

/**
 * @author jinmengyu
 * @date 2024-02-21
 */
@Data
public class TunnelStatusKafkaDto {

    private Long tunnelId;

    private Integer tunnelStatus;
}
