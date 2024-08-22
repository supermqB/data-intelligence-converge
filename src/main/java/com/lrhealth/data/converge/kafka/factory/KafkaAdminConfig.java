package com.lrhealth.data.converge.kafka.factory;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author jinmengyu
 * @date 2024-08-20
 */
@Configuration
public class KafkaAdminConfig {

    /**
     * 注入一个kafka管理实例
     *
     * @return kafka管理对象
     */
    @Bean
    public AdminClient adminClient() {
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "172.16.29.48:9092");
        return AdminClient.create(properties);
    }
}
