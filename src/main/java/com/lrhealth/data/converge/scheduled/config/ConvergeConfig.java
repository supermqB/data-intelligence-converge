package com.lrhealth.data.converge.scheduled.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author zhaohui
 * @version 1.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "lrhealth.converge")
public class ConvergeConfig {

    private String privateKeyStr;

    private String scheduledCron;

    private String outputPath;

    private int timeOut;
}
