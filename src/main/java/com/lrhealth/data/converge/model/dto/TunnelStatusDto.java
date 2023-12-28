package com.lrhealth.data.converge.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @author jinmengyu
 * @date 2023-09-20
 */
@Data
public class TunnelStatusDto {

    private Long tunnelId;

    private Integer tunnelStatus;

    private String dbStatus;

    private String lastSuccessTime;

    private List<TaskStatusDto> taskStatusList;
}
