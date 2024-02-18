package com.lrhealth.data.converge.kafka;

import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.enums.TunnelStatusEnum;
import com.lrhealth.data.converge.dao.entity.ConvFeNode;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.service.ConvFeNodeService;
import com.lrhealth.data.converge.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.model.dto.FepScheduledDto;
import com.lrhealth.data.converge.model.dto.TunnelMessageDTO;
import com.lrhealth.data.converge.service.DirectConnectCollectService;
import com.lrhealth.data.converge.service.FeTunnelConfigService;
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
    @Value("${server.port}")
    private String port;
    @Resource(name = "kafkaTemplate")
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${spring.kafka.topic.fep.tunnel-config-change}")
    private String tunnelConfigTopic;
    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private DirectConnectCollectService directConnectCollectService;
    @Resource
    private ConvFeNodeService feNodeService;
    @Resource
    private FeTunnelConfigService tunnelConfigService;

    @KafkaListener(topics = "${spring.kafka.topic.tunnel-datasource-change}")
    public void getFepTunnelConfig(@Payload String msgBody, Acknowledgment acknowledgment){
        log.info("====================receive tunnel-config msgBody={}", msgBody);
        try {
            List<Long> tunnelList = JSON.parseArray(msgBody, Long.class);
            for (Long tunnelId : tunnelList){
                ConvTunnel tunnel = tunnelService.getTunnelWithoutDelFlag(tunnelId);
                if (ObjectUtil.isNull(tunnel)) break;
                String ip;
                String fepPort;
                if (tunnel.getFrontendId() == -1){
                    log.info("直连管道{}配置被修改", tunnel.getId());
                    ip = System.getProperty("converge.ip");
                    fepPort = port;
                }else {
                    ConvFeNode convFeNode = feNodeService.getById(tunnel.getFrontendId());
                    if (ObjectUtil.isNull(convFeNode)) break;
                    ip = convFeNode.getIp();
                    fepPort = String.valueOf(convFeNode.getPort());
                }
                TunnelMessageDTO tunnelMessage = tunnelConfigService.getTunnelMessage(tunnel);
                if (tunnel.getDelFlag() == 1){
                    tunnelMessage.setStatus(TunnelStatusEnum.ABANDON.getValue());
                }
                String topic = tunnelConfigTopic + CharPool.DASHED + ip + CharPool.DASHED + fepPort;
                kafkaTemplate.send(topic, JSON.toJSONString(tunnelMessage));
            }
        } catch (Exception e) {
            log.error("tunnel config error, {}", ExceptionUtils.getStackTrace(e));
        } finally {
            acknowledgment.acknowledge();
        }
    }


    @KafkaListener(topics = "${spring.kafka.topic.fep-task}")
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
            ConvFeNode convFeNode = feNodeService.getOne(new LambdaQueryWrapper<ConvFeNode>().eq(ConvFeNode::getId, tunnel.getFrontendId()));
            // 暂时通过前置机网络情况进行添加任务
            String key = convFeNode.getIp() + "-" + convFeNode.getPort() + "-" + dto.getTunnelId() + "-" + dto.getTaskId();
//            convCache.putObject(key, dto);
        } catch (Exception e) {
            log.error("task scheduled collect error, {}", ExceptionUtils.getStackTrace(e));
        } finally {
            acknowledgment.acknowledge();
        }
    }

}
