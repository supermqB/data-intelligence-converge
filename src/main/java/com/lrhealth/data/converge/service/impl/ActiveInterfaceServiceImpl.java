package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.text.CharPool;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import com.lrhealth.data.converge.common.enums.TunnelStatusEnum;
import com.lrhealth.data.converge.common.util.thread.AsyncFactory;
import com.lrhealth.data.converge.dao.entity.ConvActiveInterfaceConfig;
import com.lrhealth.data.converge.dao.entity.ConvTask;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.service.ConvActiveInterfaceConfigService;
import com.lrhealth.data.converge.dao.service.ConvTaskService;
import com.lrhealth.data.converge.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.kafka.factory.KafkaConsumerContext;
import com.lrhealth.data.converge.kafka.factory.KafkaDynamicConsumerFactory;
import com.lrhealth.data.converge.service.ActiveInterfaceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;


@Service
@Slf4j
public class ActiveInterfaceServiceImpl implements ActiveInterfaceService {
    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private ConvTaskService taskService;
    @Resource
    private ConvActiveInterfaceConfigService activeInterfaceConfigService;
    private static final String ACTIVE_INTERFACE_GROUP_ID = "active_interface";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Resource
    private KafkaDynamicConsumerFactory dynamicConsumerFactory;

    private static final String TOPIC_PREFIX = "interface-collection-data-tunnelId";

    @Resource
    private KafkaConsumerContext consumerContext;
    @PostConstruct
    private void initialRegistry(){
        // 初始化主动接口采集消费者
        List<ConvTunnel> queueTunnels = tunnelService.list(new LambdaQueryWrapper<ConvTunnel>()
                .eq(ConvTunnel::getConvergeMethod, TunnelCMEnum.ACTIVE_INTERFACE_MODE.getCode())
                .notIn(ConvTunnel::getStatus, TunnelStatusEnum.PAUSE.getValue(), TunnelStatusEnum.ABANDON.getValue())
                .ne(ConvTunnel::getDelFlag, 1));
        for (ConvTunnel convTunnel : queueTunnels){
            initInterfaceDataConsumer(convTunnel);
        }
    }
    private void startConsumer(String broker, String topic, String topicKey){
        KafkaConsumer<Object, Object> consumer = dynamicConsumerFactory.createConsumer(topic, ACTIVE_INTERFACE_GROUP_ID, broker);
        log.info("主动接口采集kafka消费者创建成功！, consumer={}", consumer);
        consumerContext.addActiveInterfaceConsumerTask(topicKey, consumer);
    }

    public void interfaceDataSave() {

    }

    @Override
    public void activeInterfaceHandler(String topicKey, List<String> value) {
        log.info("主动接口数据处理开始！, topicKey={}, value={}", topicKey, value);
        //TODO 处理采集的数据，准备存入数据库中
    }
    private static String getTopic(ConvTunnel tunnel) {
        return  TOPIC_PREFIX + tunnel.getId().toString() ;
    }
    @Override
    public void initInterfaceDataConsumer(ConvTunnel tunnel) {
        String topic = getTopic(tunnel);
        //创建唯一的topic，启动消费者监听
        String topicKey = tunnel.getId().toString()  + CharPool.DASHED + topic;
        Integer status = tunnel.getStatus();
        switch (status){
            case 1:
                ConvTask task = taskService.createTask(tunnel, false);
                startConsumer(bootstrapServers,topic,topicKey);
                AsyncFactory.convTaskLog(task.getId(), "消费者创建成功！");
                break;
            case 2:
                log.info("接口采集[{}]正在执行中，不重复添加创建！", tunnel.getId());
                break;
            case 3:
            case 4:
                consumerContext.removeConsumerTask(topicKey);
                break;
            default:
                log.error("status={}, 不是正常的管道状态", status);
        }

    }

}
