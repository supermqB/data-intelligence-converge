package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.ConvTunnel;

import java.util.List;

/**
 * @author jinmengyu
 * @date 2024-08-21
 */
public interface MessageQueueService {

    /**
     * 消息队列采集消费者创建
     * 
     * @param tunnel
     */
    void queueModeCollect(ConvTunnel tunnel);

    /**
     * 消息队列采集执行逻辑
     */
    void messageQueueHandle(String topicKey, List<String> msgBody);

    /**
     * CDC 同步的队列落库
     */
    void cdcDbSaveQueue(ConvTunnel tunnel);

    void consolidateQueue(ConvTunnel tunnel);
}
