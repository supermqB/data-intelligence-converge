package com.lrhealth.data.converge.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author jinmengyu
 * @date 2024-12-03
 */
@Data
@Component
@ConfigurationProperties(prefix = "message.sql")
public class MessageSqlFormat {

    private String path;

    private String insertSuf;

    private String updateSuf;

    private String deleteSuf;

    private String manageSuf;
}
