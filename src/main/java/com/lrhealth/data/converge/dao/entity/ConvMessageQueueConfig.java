package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

/**
 * @author jinmengyu
 * @date 2024-08-12
 */
@Data
public class ConvMessageQueueConfig {

    /**
     * 自增主键
     */
    private Long id;

    private String queueType;

    /**
     * kafka broker地址
     */
    private String kafkaBroker;

    /**
     * kafka topic
     */
    private String kafkaTopic;

    /**
     * kafka consumer group id
     */
    private String kafkaGroupId;

    /**
     * kafka group id
     */
    private String topicOffset;

    /**
     * 消费者并发数需要和 partition 数量一致
     */
    private Integer concurrency;

    /**
     * 消费者 bean 方法
     */
    private String beanMethod;

    /**
     * kafka 配置
     */
    private String kafkaConfig;

    /**
     * 运行状态
     */
    private Integer runStatus;

    private String username;

    private String password;

    @TableLogic
    private Integer delFlag;
}
