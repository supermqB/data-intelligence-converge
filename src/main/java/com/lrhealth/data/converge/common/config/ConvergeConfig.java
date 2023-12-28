package com.lrhealth.data.converge.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
