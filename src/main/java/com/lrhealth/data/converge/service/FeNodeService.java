package com.lrhealth.data.converge.service;


import com.lrhealth.data.converge.dao.entity.*;
import com.lrhealth.data.converge.model.FileTask;
import com.lrhealth.data.converge.model.dto.*;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public interface FeNodeService {

    ConvTunnel updateTunnel(TunnelStatusKafkaDto tunnelStatusDto);

    ConvTask saveOrUpdateTask(TaskInfoKafkaDto taskInfoKafkaDto, ConvTunnel tunnel,ConvTask oldTask);

    void saveOrUpdateLog(List<TaskLogDto> taskLogs, Integer taskId);

    ConvTaskResultView saveOrUpdateFile(ResultViewInfoDto resultViewInfoDto, Integer taskId);


    ConvTaskResultFile saveOrUpdateFile(ResultFileInfoDto resultFileInfoDto, ConvTask convTask);

    ConvTaskResultInterface saveOrUpdateInterface(ResultInterfaceDTO resultFileInfoDto, ConvTask convTask);

    void updateTaskResultView(ConcurrentLinkedDeque<FileTask> taskDeque, List<ResultViewInfoDto> resultViewInfoDtoList, ConvTask convTask);

    void updateTaskResultInterface( List<ResultInterfaceDTO> resultViewInfoDtoList, ConvTask convTask);

    void updateTaskResultFile(ConcurrentLinkedDeque<FileTask> taskDeque, List<ResultFileInfoDto> resultFileInfoDtoList, ConvTask convTask);
}
