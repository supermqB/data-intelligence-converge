package com.lrhealth.data.converge.service;


import com.lrhealth.data.converge.common.exception.FeNodeStatusException;
import com.lrhealth.data.converge.common.exception.PingException;
import com.lrhealth.data.converge.dao.entity.*;
import com.lrhealth.data.converge.model.dto.*;

import java.util.List;

public interface FeNodeService {

    public boolean ping(String ip, String port);

    public boolean recover(PingException e);

    public FrontendStatusDto getFeNodeStatus(ConvFeNode node);

    public FrontendStatusDto recover(FeNodeStatusException e);

    ConvTunnel updateTunnel(TunnelStatusDto tunnelStatusDto);

    ConvTask saveOrUpdateTask(TaskStatusDto taskStatusDto, ConvTunnel tunnel,ConvTask oldTask);

    void saveOrUpdateLog(List<TaskLogDto> taskLogs, ConvTask convTask);

    ConvTaskResultView saveOrUpdateFile(ResultViewInfoDto resultViewInfoDto, ConvTask convTask);

    ConvTaskResultCdc saveOrUpdateFile(ResultCDCInfoDTO cdcInfoDTO, ConvTask convTask);

    ConvTaskResultFile saveOrUpdateFile(ResultFileInfoDto resultFileInfoDto, ConvTask convTask);
}
