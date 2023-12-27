package com.lrhealth.data.converge.common.init;

import com.lrhealth.data.converge.common.config.db.DataSourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * @author yuanbaiyu
 * @since 2022/11/16 14:26
 */
@Slf4j
@Component
public class InitBootCacheRunner implements ApplicationRunner {



    @Resource
    DataSourceRepository dataSourceRepository;

    @Override
    public void run(ApplicationArguments args) {
        dataSourceRepository.initOdsDataSource();
    }


}
