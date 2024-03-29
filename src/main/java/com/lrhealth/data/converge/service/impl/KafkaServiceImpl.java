package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.lrhealth.data.common.enums.conv.KafkaSendFlagEnum;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.ConvFeNode;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.dao.service.ConvFeNodeService;
import com.lrhealth.data.converge.model.dto.DsKafkaDto;
import com.lrhealth.data.converge.model.dto.IncrSequenceDto;
import com.lrhealth.data.converge.service.KafkaService;
import com.lrhealth.data.converge.service.XdsInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * @author jinmengyu
 * @date 2023-10-12
 */
@Slf4j
@Service
public class KafkaServiceImpl implements KafkaService {

    @Value("${server.port}")
    private String port;

    @Value("${spring.kafka.topic.xds}")
    private String xdsTopic;

    @Value("${spring.kafka.topic.dolphinScheduler-task}")
    private String dsTopic;
    @Value("${spring.kafka.topic.fep.increment-sequence}")
    private String incrSequenceTopic;
    @Resource
    private XdsInfoService xdsInfoService;
    @Resource
    private ConvFeNodeService feNodeService;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 发送kafka消息
     *
     * @param xds XDS信息
     */
    @Override
    public void xdsSendKafka(Xds xds) {
        if (ObjectUtil.isNull(xds)){
            throw new CommonException("kafka send xds is null");
        }
        Xds checkXds = xdsInfoService.getById(xds.getId());
        if (!KafkaSendFlagEnum.isSent(xds.getKafkaSendFlag())) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", checkXds.getId());
            kafkaTemplate.send(xdsTopic, JSON.toJSONString(map));
            xdsInfoService.updateKafkaSent(xds);
        }
    }

    @Override
    public void dsSendKafka(DsKafkaDto dto) {
        if (ObjectUtil.isNull(dto)){
            throw new CommonException("kafka send ds is null");
        }
        kafkaTemplate.send(dsTopic, JSON.toJSONString(dto));
    }

    @Override
    public void updateFepIncrSequence(IncrSequenceDto incrSequenceDto, ConvTunnel tunnel) {
        // topic处理
        String topic = incrSequenceTopic + CharPool.DASHED + topicSuffixIpPort(tunnel.getId(), tunnel.getFrontendId());
        kafkaTemplate.send(topic, JSON.toJSONString(incrSequenceDto));
    }

    @Override
    public String topicSuffixIpPort(Long tunnelId, Long frontendId) {
        String ip;
        String fepPort;
        if (frontendId == -1){
            log.info("直连管道{}配置被修改", tunnelId);
            ip = System.getProperty("converge.ip");
            fepPort = port;
        }else {
            ConvFeNode convFeNode = feNodeService.getById(frontendId);
            ip = convFeNode.getIp();
            fepPort = String.valueOf(convFeNode.getPort());
        }
        return ip + CharPool.DASHED + fepPort;
    }
}
