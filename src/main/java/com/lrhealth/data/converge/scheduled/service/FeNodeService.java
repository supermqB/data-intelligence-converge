package com.lrhealth.data.converge.scheduled.service;

import com.lrhealth.data.converge.scheduled.config.exception.FeNodeStatusException;
import com.lrhealth.data.converge.scheduled.config.exception.PingException;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvFeNode;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTask;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultCdc;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultView;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.model.dto.*;

public interface FeNodeService {

    public boolean ping(String ip, String port);

    public boolean recover(PingException e);

    public FrontendStatusDto getFeNodeStatus(ConvFeNode node);

    public FrontendStatusDto recover(FeNodeStatusException e);

    ConvTunnel updateTunnel(TunnelStatusDto tunnelStatusDto);

    ConvTask saveOrUpdateTask(TaskStatusDto taskStatusDto, ConvTunnel tunnel);

    void saveOrUpdateLog(TaskLogDto taskLog, ConvTask convTask);

    ConvTaskResultView saveOrUpdateFile(ResultViewInfoDto resultViewInfoDto, ConvTask convTask);

    ConvTaskResultCdc saveOrUpdateFile(ResultCDCInfoDTO cdcInfoDTO, ConvTask convTask);
}
