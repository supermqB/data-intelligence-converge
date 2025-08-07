package com.lrhealth.data.converge.kafka.factory;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Properties;

/**
 * @author jinmengyu
 * @date 2024-08-20
 */
@Component
public class KafkaDynamicConsumerFactory {

    /**
     * 创建消费者
     * 
     * @param topic   订阅主题
     * @param groupId 消费者组
     * @param broker  kafka-broker地址
     * @return 消费者对象
     */
    public <K, V> KafkaConsumer<K, V> createConsumer(String topic, String groupId, String broker) {
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProperties.put("max.poll.records", 1000);
        consumerProperties.put("fetch.max.bytes", 128 * 1024 * 1024);
        consumerProperties.put("max.partition.fetch.bytes", 2 * 1024 * 1024);
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // 从最早开始消费
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // 信任所有类型以反序列化
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // 禁用自动提交
        consumerProperties.put("spring.json.trusted.packages", "*");
        // 新建消费者
        KafkaConsumer<K, V> consumer = new KafkaConsumer<>(consumerProperties);
        // 使消费者订阅对应主题
        consumer.subscribe(Collections.singleton(topic));
        return consumer;
    }

    public void cancelConsumer(String broker, String topic) {
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        AdminClient adminClient = AdminClient.create(properties);
        adminClient.deleteTopics(Collections.singleton(topic));
    }
}
