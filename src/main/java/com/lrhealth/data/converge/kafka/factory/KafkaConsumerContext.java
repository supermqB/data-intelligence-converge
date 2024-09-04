package com.lrhealth.data.converge.kafka.factory;

import com.lrhealth.data.converge.service.MessageQueueService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;


/**
 * @author jinmengyu
 * @date 2024-08-20
 */
@Slf4j
@Component
public class KafkaConsumerContext {

    private KafkaConsumerContext(){}

    /**
     * 存放所有自己创建的Kafka消费者任务
     * key: groupId
     * value: kafka消费者任务
     */
    private static final Map<String, KafkaConsumer<?, ?>> consumerMap = new ConcurrentHashMap<>();

    /**
     * 存放所有定时任务的哈希表
     * key: groupId
     * value: 定时任务对象，用于定时执行kafka消费者的消息消费任务
     */
    private static final Map<String, ScheduledFuture<?>> scheduleMap = new ConcurrentHashMap<>();

    /**
     * 任务调度器，用于定时任务
     */
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(24);

    @Resource
    private MessageQueueService queueService;

    /**
     * 添加一个Kafka消费者任务
     *
     * @param topicKey    消费者主题
     * @param consumer 消费者对象
     * @param <K>      消息键类型
     * @param <V>      消息值类型
     */
    public <K, V> void addConsumerTask(String topicKey, KafkaConsumer<K, V> consumer) {
        // 存入消费者列表
        consumerMap.put(topicKey, consumer);
        // 创建定时任务，每隔1s拉取消息并处理
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {
            // 每次执行拉取消息之前，先检查订阅者是否已被取消（如果订阅者不存在于订阅者列表中说明被取消了）
            // 因为Kafka消费者对象是非线程安全的，因此在这里把取消订阅的逻辑和拉取并处理消息的逻辑写在一起并放入定时器中，判断列表中是否存在消费者对象来确定是否取消任务
            if (!consumerMap.containsKey(topicKey)) {
                // 取消订阅并关闭消费者
                consumer.unsubscribe();
                consumer.close();
                // 关闭定时任务
                scheduleMap.remove(topicKey).cancel(true);
                return;
            }
            // 拉取消息
            ConsumerRecords<K, V> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<K, V> record : records) {
                // 自定义处理每次拉取的消息逻辑
                String topicName = record.topic();
                String msgBody = (String) record.value();
                log.info("receive kafka data, topic=[{}], value=[{}]", topicName,  msgBody);
                try {
                    queueService.messageQueueHandle(topicKey, msgBody);
                }catch (Exception e){
                    log.error(ExceptionUtils.getStackTrace(e));
                }

            }
        }, 0, 1, TimeUnit.SECONDS);
        // 将任务存入对应的列表以后续管理
        scheduleMap.put(topicKey, future);
    }

    /**
     * 移除Kafka消费者定时任务并关闭消费者订阅
     *
     * @param topic 消费者的主题
     */
    public void removeConsumerTask(String topic) {
        if (!consumerMap.containsKey(topic)) {
            return;
        }
        // 从列表中移除消费者
        consumerMap.remove(topic);
    }
}
