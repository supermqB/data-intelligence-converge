package com.lrhealth.data.converge.kafka.factory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.service.ActiveInterfaceService;
import com.lrhealth.data.converge.service.FileCollectService;
import com.lrhealth.data.converge.service.MessageQueueService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author jinmengyu
 * @date 2024-08-20
 */
@Slf4j
@Component
public class KafkaConsumerContext {
    @Resource
    private Executor dataSinkThreadPool;

    private KafkaConsumerContext() {
    }

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
    private static final ScheduledExecutorService activeExecutor = Executors.newScheduledThreadPool(24);

    @Resource
    private MessageQueueService queueService;

    @Resource
    private ActiveInterfaceService activeInterfaceService;

    @Resource
    private FileCollectService fileCollectService;

    /**
     * 添加一个Kafka消费者任务
     *
     * @param topicKey 消费者主题
     * @param consumer 消费者对象
     * @param <K>      消息键类型
     * @param <V>      消息值类型
     */
    public <K, V> void addConsumerTask(String topicKey, KafkaConsumer<K, V> consumer) {
        // 存入消费者列表
        consumerMap.put(topicKey, consumer);
        log.info("创建定时任务: key={}", topicKey);
        // 创建定时任务，每隔30s拉取消息并处理
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {
            // 每次执行拉取消息之前，先检查订阅者是否已被取消（如果订阅者不存在于订阅者列表中说明被取消了）
            // 因为Kafka消费者对象是非线程安全的，因此在这里把取消订阅的逻辑和拉取并处理消息的逻辑写在一起并放入定时器中，判断列表中是否存在消费者对象来确定是否取消任务
            if (!consumerMap.containsKey(topicKey)) {
                log.info("kafka unsubscribe topic [{}]", topicKey);
                // 取消订阅并关闭消费者
                consumer.unsubscribe();
                consumer.close();
                log.info("取消订阅，关闭消费者: key={}", topicKey);
                // 关闭定时任务
                scheduleMap.remove(topicKey).cancel(true);
                log.info("删除定时任务: key={}", topicKey);
                return;
            }
            log.info("kafka polling topic [{}]", topicKey);
            // 拉取消息
            ConsumerRecords<K, V> records = consumer.poll(Duration.ofMillis(1000));
            Map<String, List<String>> topicBodyMap = new HashMap<>();
            for (ConsumerRecord<K, V> record : records) {
                // 自定义处理每次拉取的消息逻辑
                String topicName = record.topic();
                String msgBody = (String) record.value();
                log.info("receive kafka data, topic=[{}], value=[{}]", topicName, msgBody);
                topicBodyMap.computeIfAbsent(topicName, k -> new ArrayList<>()).add(msgBody);
            }
            // 使用多线程处理整理后的表
            topicBodyMap.entrySet().stream().parallel()
                    .forEach(map -> queueService.messageQueueHandle(topicKey, map.getValue()));
            consumer.commitSync();
        }, 0, 30, TimeUnit.SECONDS);
        // 将任务存入对应的列表以后续管理
        scheduleMap.put(topicKey, future);
    }

    private Map<Long, ConvTunnel> waitDataMergeTunnels = new LinkedHashMap<>();

    public void addTableMergeTask(ConvTunnel tunnel) {
        waitDataMergeTunnels.put(tunnel.getId(), tunnel);
    }

    @Scheduled(cron = "0 * * * * ?") // 每日凌晨1点
    public void dailyTaskDataMergeTask() {
        log.info("dailyTaskDataMergeTask is starting...");
        waitDataMergeTunnels.forEach((tid, t) -> {
            log.info("start consolidate tunnel {}", tid);
            queueService.consolidateQueue(t);
        });
    }

    public <K, V> void addActiveInterfaceConsumerTask(String topicKey, KafkaConsumer<K, V> consumer) {
        // 存入消费者列表
        consumerMap.put(topicKey, consumer);
        log.info("创建主动接口采集消费定时任务: key={}", topicKey);

        // 创建容量为1000的线程安全阻塞队列
        BlockingQueue<String> topicBodyQueue = new LinkedBlockingQueue<>();
        // 创建定时任务，每隔10s拉取消息并处理
        ScheduledFuture<?> future = activeExecutor.scheduleAtFixedRate(() -> {
            // 检查消费者是否已被移除
            if (!consumerMap.containsKey(topicKey)) {
                // 清理资源
                consumer.unsubscribe();
                consumer.close();
                scheduleMap.remove(topicKey).cancel(true);
                log.info("消费者已关闭并删除定时任务: key={}", topicKey);
                return;
            }
            try {
                // 拉取消息，最多等待1秒钟，配置最多一次拉取1000条
                ConsumerRecords<K, V> records = consumer.poll(Duration.ofMillis(1000));

                // 如果本次无新消息，但队列中仍有数据，则触发一次处理
                if (records.isEmpty()) {
                    if (!topicBodyQueue.isEmpty()) {
                        flushQueue(topicKey, topicBodyQueue);
                    }
                    return;
                }
                // 处理拉取到的消息
                for (ConsumerRecord<K, V> record : records) {
                    String msgBody = (String) record.value();
                    log.info("interface collector receive kafka data, topic=[{}], value=[{}]", record.topic(), msgBody);
                    try {
                        if (topicBodyQueue.size() == 1000) {
                            flushQueue(topicKey, topicBodyQueue); // 触发处理并清空队列
                        }
                        topicBodyQueue.add(msgBody);
                    } catch (Exception e) {
                        log.error("插入队列失败: {}", e.getMessage());
                    }
                }
                consumer.commitSync();
            } catch (Exception e) {
                log.error("处理 Kafka 消息时发生异常: {}", e.getMessage(), e);
            }
        }, 0, 10, TimeUnit.SECONDS);
        scheduleMap.put(topicKey, future);
    }

    public <K, V> void addFileCollectConsumerTask(String topicKey, KafkaConsumer<K, V> consumer) {
        // 存入消费者列表
        consumerMap.put(topicKey, consumer);
        log.info("创建文件采集消费定时任务: key={}", topicKey);

        // 创建容量为1000的线程安全阻塞队列
        BlockingQueue<String> topicBodyQueue = new LinkedBlockingQueue<>();
        // 创建定时任务，每隔10s拉取消息并处理
        ScheduledFuture<?> future = activeExecutor.scheduleAtFixedRate(() -> {
            // 检查消费者是否已被移除
            if (!consumerMap.containsKey(topicKey)) {
                // 清理资源
                consumer.unsubscribe();
                consumer.close();
                scheduleMap.remove(topicKey).cancel(true);
                log.info("消费者已关闭并删除定时任务: key={}", topicKey);
                return;
            }
            try {
                // 拉取消息，最多等待1秒钟，配置最多一次拉取1000条
                ConsumerRecords<K, V> records = consumer.poll(Duration.ofMillis(1000));

                // 如果本次无新消息，但队列中仍有数据，则触发一次处理
                if (records.isEmpty()) {
                    if (!topicBodyQueue.isEmpty()) {
                        flushQueueData(topicKey, topicBodyQueue);
                    }
                    return;
                }
                // 处理拉取到的消息
                for (ConsumerRecord<K, V> record : records) {
                    String msgBody = (String) record.value();
                    log.info("file collector receive kafka data, topic=[{}], count=[{}]", record.topic(),
                            records.count());
                    try {
                        if (topicBodyQueue.size() == 1000) {
                            flushQueueData(topicKey, topicBodyQueue); // 触发处理并清空队列
                        }
                        topicBodyQueue.add(msgBody);
                    } catch (Exception e) {
                        log.error("插入队列失败: {}", e.getMessage());
                    }
                }
                consumer.commitSync();
            } catch (Exception e) {
                log.error("处理 Kafka 消息时发生异常: {}", e.getMessage(), e);
            }
        }, 0, 30, TimeUnit.SECONDS);
        scheduleMap.put(topicKey, future);
    }

    /**
     * 提交队列中的数据并清空
     */
    private void flushQueue(String topicKey, BlockingQueue<String> queue) {
        if (queue.isEmpty())
            return;
        List<String> dataList = new ArrayList<>(queue);
        queue.clear();
        CompletableFuture.runAsync(() -> {
            try {
                activeInterfaceService.activeInterfaceHandler(topicKey, dataList);
                log.info("异步入库接口采集批次数量: {}", dataList.size());
            } catch (Exception e) {
                log.error("异步入库接口采集数据失败: {}", e.getMessage());
            }
        }, dataSinkThreadPool);
    }

    /**
     * 提交队列中的数据并清空
     */
    private void flushQueueData(String topicKey, BlockingQueue<String> queue) {
        if (queue.isEmpty())
            return;
        List<String> dataList = new ArrayList<>(queue);
        queue.clear();
        CompletableFuture.runAsync(() -> {
            try {
                fileCollectService.fileDataHandler(topicKey, dataList);
                log.info("异步提交文件采集批次数量: {}", dataList.size());
            } catch (Exception e) {
                log.error("异步提交文件采集数据失败: {}", e.getMessage());
            }
        }, dataSinkThreadPool);
    }

    /**
     * 移除Kafka消费者定时任务并关闭消费者订阅
     *
     * @param topicKey 消费者的主题
     */
    public void removeConsumerTask(String topicKey) {
        if (!consumerMap.containsKey(topicKey)) {
            return;
        }
        // 从列表中移除消费者
        KafkaConsumer<?, ?> consumer = consumerMap.get(topicKey);
        // 取消订阅并关闭消费者
        consumer.unsubscribe();
        consumer.close();
        consumerMap.remove(topicKey);
        log.info("取消订阅，关闭消费者: key={}", topicKey);
        // 关闭定时任务
        scheduleMap.remove(topicKey).cancel(true);
        log.info("删除定时任务: key={}", topicKey);
    }
}
