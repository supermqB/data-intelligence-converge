package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.dto.DsKafkaDto;
import com.lrhealth.data.converge.model.dto.IncrSequenceDto;

/**
 * @author jinmengyu
 * @date 2023-10-12
 */
public interface KafkaService {

    void xdsSendKafka(Xds xds);

    void dsSendKafka(DsKafkaDto dto);

    void updateFepIncrSequence(IncrSequenceDto incrSequenceDto, ConvTunnel tunnel);

    String topicSuffixIpPort(Long tunnelId, Long frontendId);
}
