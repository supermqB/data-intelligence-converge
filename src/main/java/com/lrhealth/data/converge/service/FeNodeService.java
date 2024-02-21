package com.lrhealth.data.converge.service;


import com.lrhealth.data.converge.common.exception.FeNodeStatusException;
import com.lrhealth.data.converge.common.exception.PingException;
import com.lrhealth.data.converge.dao.entity.*;
import com.lrhealth.data.converge.model.FileTask;
import com.lrhealth.data.converge.model.dto.*;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public interface FeNodeService {

    public boolean ping(String ip, String port);

    public boolean recover(PingException e);

    public FrontendStatusDto getFeNodeStatus(ConvFeNode node);

    public FrontendStatusDto recover(FeNodeStatusException e);

    ConvTunnel updateTunnel(TunnelStatusKafkaDto tunnelStatusDto);

    ConvTask saveOrUpdateTask(TaskInfoKafkaDto taskInfoKafkaDto, ConvTunnel tunnel,ConvTask oldTask);

    void saveOrUpdateLog(List<TaskLogDto> taskLogs, Integer taskId);

    ConvTaskResultView saveOrUpdateFile(ResultViewInfoDto resultViewInfoDto, Integer taskId);

    ConvTaskResultCdc saveOrUpdateFile(ResultCDCInfoDTO cdcInfoDTO, ConvTask convTask);

    ConvTaskResultFile saveOrUpdateFile(ResultFileInfoDto resultFileInfoDto, ConvTask convTask);

    void updateTaskResultView(ConcurrentLinkedDeque<FileTask> taskDeque, List<ResultViewInfoDto> resultViewInfoDtoList, ConvTask convTask);

    void updateTaskResultFile(ConcurrentLinkedDeque<FileTask> taskDeque, List<ResultFileInfoDto> resultFileInfoDtoList, ConvTask convTask);
}
