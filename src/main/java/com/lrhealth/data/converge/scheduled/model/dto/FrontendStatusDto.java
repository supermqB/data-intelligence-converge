package com.lrhealth.data.converge.scheduled.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @author jinmengyu
 * @date 2023-09-20
 */
@Data
public class FrontendStatusDto {

    private Integer frontendStatus;

    private List<TunnelStatusDto> tunnelStatusDtoList;
}
