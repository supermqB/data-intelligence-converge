package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.dto.DsKafkaDto;
import com.lrhealth.data.converge.model.dto.IncrSequenceDto;

import java.util.List;

/**
 * @author jinmengyu
 * @date 2023-10-12
 */
public interface KafkaService {

    void xdsSendKafka(Xds xds);

    void dsSendKafka(DsKafkaDto dto);

    void updateFepIncrSequence(IncrSequenceDto incrSequenceDto, ConvTunnel tunnel);

    /**
     * 发送给前置机的消息后缀
     * 格式： ip-port
     * @param frontendId 前置机id
     * @return 处理过后的格式
     */
    String topicSuffixIpPort(Long frontendId);

    void dsConfigSendFep(String sendMsg, List<Long> feNodeList);
}
