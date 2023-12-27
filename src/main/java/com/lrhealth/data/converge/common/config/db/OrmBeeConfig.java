package com.lrhealth.data.converge.common.config.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.teasoft.honey.osql.core.BeeFactory;
import org.teasoft.honey.osql.core.HoneyConfig;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 配置orm-bee框架的数据源
 */
@Slf4j
@Component
public class OrmBeeConfig {

    @PostConstruct
    public void init() {
        HoneyConfig honeyConfig = HoneyConfig.getHoneyConfig();
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
        BeeFactory.getInstance().setDataSourceMap(dataSourceMap);
        //使用多数据源  多个数据源时必须设置
        honeyConfig.multiDS_enable = true;
        //同时要使用多种数据库,一定要设置
        honeyConfig.multiDS_differentDbType=true;
        honeyConfig.setLoggerType("log4j2");
        honeyConfig.naming_translateType = 3;
    }

}

