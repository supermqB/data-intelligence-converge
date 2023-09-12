package com.lrhealth.data.converge.common.config.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.teasoft.honey.osql.core.BeeFactory;
import org.teasoft.honey.osql.core.HoneyConfig;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;


@Slf4j
@Component
@DependsOn({"odsDataSource"})
public class InitOrmBeeConfig {

    @Resource
    private DataSource odsDataSource;

    @PostConstruct
    public void init() {
        HoneyConfig honeyConfig = HoneyConfig.getHoneyConfig();
        BeeFactory.getInstance().setDataSource(odsDataSource);
        honeyConfig.setLoggerType("log4j2");
        honeyConfig.naming_translateType = 3;
        honeyConfig.showSQL=true;
    }

}

