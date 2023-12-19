package com.lrhealth.data.converge.scheduled.service;

import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.model.dto.ActiveFepUploadDto;
import com.lrhealth.data.converge.scheduled.model.dto.TunnelMessageDTO;

import java.util.List;

/**
 * @author jinmengyu
 * @date 2023-11-14
 */
public interface FeTunnelConfigService {

    List<TunnelMessageDTO> getFepTunnelConfig(String ip, Integer port);

    void updateFepStatus(ActiveFepUploadDto activeFepUploadDto);

    TunnelMessageDTO getTunnelMessage(ConvTunnel tunnel);
}
