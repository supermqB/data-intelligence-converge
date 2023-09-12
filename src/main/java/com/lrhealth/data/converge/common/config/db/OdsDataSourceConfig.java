package com.lrhealth.data.converge.common.config.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * ods数据源配置
 */
@Slf4j
@Configuration
public class OdsDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.ods")
    public DataSource odsDataSource() {
        return DataSourceBuilder.create().build();
    }

}