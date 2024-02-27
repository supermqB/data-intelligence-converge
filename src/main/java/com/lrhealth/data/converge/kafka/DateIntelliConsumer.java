package com.lrhealth.data.converge.kafka;

import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.enums.TunnelStatusEnum;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.service.ConvOdsDatasourceConfigService;
import com.lrhealth.data.converge.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.model.dto.DataSourceInfoDto;
import com.lrhealth.data.converge.model.dto.DataSourceParamDto;
import com.lrhealth.data.converge.model.dto.FepScheduledDto;
import com.lrhealth.data.converge.model.dto.TunnelMessageDTO;
import com.lrhealth.data.converge.service.DirectConnectCollectService;
import com.lrhealth.data.converge.service.FeTunnelConfigService;
import com.lrhealth.data.converge.service.KafkaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author jinmengyu
 * @date 2024-02-04
 */
@Slf4j
@Component
public class DateIntelliConsumer {
    @Resource(name = "kafkaTemplate")
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${spring.kafka.topic.fep.tunnel-config-change}")
    private String tunnelConfigTopic;
    @Value("${spring.kafka.topic.fep.tunnel-schedule-task}")
    private String tunnelScheduleTopic;
    @Value("${spring.kafka.topic.fep.original-structure-config}")
    private String originalStructureTopic;
    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private DirectConnectCollectService directConnectCollectService;
    @Resource
    private FeTunnelConfigService tunnelConfigService;
    @Resource
    private ConvOdsDatasourceConfigService odsDatasourceConfigService;
    @Resource
    private KafkaService kafkaService;

    @KafkaListener(topics = "${spring.kafka.topic.intelligence.tunnel-datasource-change}")
    public void getFepTunnelConfig(@Payload String msgBody, Acknowledgment acknowledgment){
        log.info("====================receive tunnel-config msgBody={}", msgBody);
        try {
            List<Long> tunnelList = JSON.parseArray(msgBody, Long.class);
            for (Long tunnelId : tunnelList){
                ConvTunnel tunnel = tunnelService.getTunnelWithoutDelFlag(tunnelId);
                if (ObjectUtil.isNull(tunnel)) break;
                String topicSuffix = kafkaService.topicSuffixIpPort(tunnelId, tunnel.getFrontendId());
                TunnelMessageDTO tunnelMessage = tunnelConfigService.getTunnelMessage(tunnel);
                if (tunnel.getDelFlag() == 1){
                    tunnelMessage.setStatus(TunnelStatusEnum.ABANDON.getValue());
                }
                String topic = tunnelConfigTopic + CharPool.DASHED + topicSuffix;
                kafkaTemplate.send(topic, JSON.toJSONString(tunnelMessage));
            }
        } catch (Exception e) {
            log.error("tunnel config error, {}", ExceptionUtils.getStackTrace(e));
        } finally {
            acknowledgment.acknowledge();
        }
    }


    @KafkaListener(topics = "${spring.kafka.topic.intelligence.fep-task}")
    public void getScheduleTask(@Payload String msgBody, Acknowledgment acknowledgment){
        log.info("====================receive datax-task msgBody={}", msgBody);
        try {
            FepScheduledDto dto = JSON.parseObject(msgBody, FepScheduledDto.class);
            ConvTunnel tunnel = tunnelService.getOne(new LambdaQueryWrapper<ConvTunnel>().eq(ConvTunnel::getId, dto.getTunnelId()));
            if (tunnel.getFrontendId() == -1){
                directConnectCollectService.tunnelConfig(TunnelMessageDTO.builder().id(tunnel.getId()).build());
                directConnectCollectService.tunnelExec(null, tunnel.getId());
                return;
            }
            String topic = tunnelScheduleTopic + CharPool.DASHED + kafkaService.topicSuffixIpPort(tunnel.getId(), tunnel.getFrontendId());
            kafkaTemplate.send(topic, JSON.toJSONString(dto));
        } catch (Exception e) {
            log.error("task scheduled collect error, {}", ExceptionUtils.getStackTrace(e));
        } finally {
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(topics = "${spring.kafka.topic.intelligence.original-structure}")
    public void getOriginalStructure(@Payload String msgBody, Acknowledgment acknowledgment){
        log.info("====================receive get-original-structure msgBody={}", msgBody);
        try {
            DataSourceParamDto dataSourceParam = JSON.parseObject(msgBody, DataSourceParamDto.class);
            List<DataSourceInfoDto> orgReaderSource = odsDatasourceConfigService.getOrgReaderSource(dataSourceParam);
            String topic = originalStructureTopic + CharPool.DASHED + dataSourceParam.getOrgCode();
            kafkaTemplate.send(topic, JSON.toJSONString(orgReaderSource));
        } catch (Exception e) {
            log.error("get original structure error, {}", ExceptionUtils.getStackTrace(e));
        } finally {
            acknowledgment.acknowledge();
        }
    }

}
