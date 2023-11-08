package com.lrhealth.data.converge.scheduled.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @author jinmengyu
 * @date 2023-09-20
 */
@Data
public class TaskStatusDto {

    private Integer taskId;

    private String startTime;

    private String endTime;

    private Integer status;

    private List<TaskLogDto> taskLogs;

    private List<ResultViewInfoDto> resultViewInfoDtoList;

    private List<ResultCDCInfoDTO> cdcInfoList;

    private List<ResultFileInfoDto> resultFileInfoDtoList;
}
