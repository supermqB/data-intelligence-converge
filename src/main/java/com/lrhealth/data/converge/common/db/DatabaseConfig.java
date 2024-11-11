package com.lrhealth.data.converge.common.db;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author jinmengyu
 * @date 2024-11-11
 */
@Configuration
public class DatabaseConfig {

    @Bean
    public DbConnectionManager dbConnectionManager(){
        return new DbConnectionManager();
    }
}
