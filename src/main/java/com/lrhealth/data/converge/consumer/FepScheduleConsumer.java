package com.lrhealth.data.converge.consumer;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.cache.Cache;
import com.lrhealth.data.converge.model.dto.FepScheduledDto;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvFeNode;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.dao.service.ConvFeNodeService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTunnelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 页面治理任务重启
 * @author jinmengyu
 * @date 2023-06-14
 */
@Slf4j
@Component
public class FepScheduleConsumer {
    @Resource
    private Cache convCache;

    @Resource
    private ConvFeNodeService feNodeService;
    @Resource
    private ConvTunnelService tunnelService;

    @KafkaListener(topics = "${spring.kafka.topic.fep-task}")
    public void consume(@Payload String msgBody) {
        try {
            FepScheduledDto dto = JSON.parseObject(msgBody, FepScheduledDto.class);
            if (ObjectUtil.isEmpty(dto)){
                log.error("kafka任务值为空");
                return;
            }
            ConvTunnel tunnel = tunnelService.getOne(new LambdaQueryWrapper<ConvTunnel>().eq(ConvTunnel::getId, dto.getTunnelId()));
            if (tunnel.getFrontendId() == -1){
                // todo: 直连，直接让汇聚执行
                return;
            }
            ConvFeNode convFeNode = feNodeService.getOne(new LambdaQueryWrapper<ConvFeNode>().eq(ConvFeNode::getId, tunnel.getFrontendId()));
            // 直接调度-管道id-任务id
            String key = convFeNode.getIp() + "-" + convFeNode.getPort() + "-" + dto.getTunnelId() + "-" + dto.getTaskId();
            convCache.putObject(key, dto);
        }catch (Exception e){
            log.error("task scheduled collect error, {}", ExceptionUtils.getStackTrace(e));
        }

    }
}
