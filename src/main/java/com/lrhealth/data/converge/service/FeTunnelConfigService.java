package com.lrhealth.data.converge.service;


import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.model.dto.TunnelMessageDTO;

/**
 * @author jinmengyu
 * @date 2023-11-14
 */
public interface FeTunnelConfigService {

    void kafkaUpdateFepStatus(String key, String msgBody);

    TunnelMessageDTO getTunnelMessage(ConvTunnel tunnel);
}
