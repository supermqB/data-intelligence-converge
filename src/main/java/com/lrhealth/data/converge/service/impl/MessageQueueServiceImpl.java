package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.lrhealth.data.converge.common.util.thread.AsyncFactory;
import com.lrhealth.data.converge.dao.entity.ConvMessageQueueConfig;
import com.lrhealth.data.converge.dao.entity.ConvTask;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.service.ConvMessageQueueConfigService;
import com.lrhealth.data.converge.dao.service.ConvTaskService;
import com.lrhealth.data.converge.kafka.factory.KafkaConsumerContext;
import com.lrhealth.data.converge.kafka.factory.KafkaDynamicConsumerFactory;
import com.lrhealth.data.converge.service.MessageQueueService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author jinmengyu
 * @date 2024-08-21
 */
@Service
@Slf4j
public class MessageQueueServiceImpl implements MessageQueueService {

    @Resource
    private ConvMessageQueueConfigService queueConfigService;

    private static final String KAFKA_GROUP_ID = "queue_collect";

    @Resource
    private KafkaDynamicConsumerFactory dynamicConsumerFactory;
    @Resource
    private ConvTaskService taskService;
    @Resource
    private KafkaConsumerContext consumerContext;

    @Override
    public void queueModeCollect(ConvTunnel tunnel) {
        // 查询队列配置
        ConvMessageQueueConfig queueConfig = queueConfigService.getById(tunnel.getMessageQueueId());
        if (ObjectUtil.isNull(queueConfig) || !"kafka".equalsIgnoreCase(queueConfig.getQueueType())){
            return;
        }
        ConvTask task = taskService.createTask(tunnel, false);
        String broker = queueConfig.getKafkaBroker();
        String topic = queueConfig.getKafkaTopic();
        KafkaConsumer<Object, Object> consumer = dynamicConsumerFactory.createConsumer(topic, KAFKA_GROUP_ID, broker);
        consumerContext.addConsumerTask(topic, consumer);
        AsyncFactory.convTaskLog(task.getId(), "消费者创建成功！");

    }

    @Override
    public void messageQueueHandle() {

    }
}
