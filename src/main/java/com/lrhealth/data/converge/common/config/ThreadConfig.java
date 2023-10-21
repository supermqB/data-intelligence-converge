package com.lrhealth.data.converge.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池
 *
 * @author lr
 * @date 2022-11-28
 */
@Configuration
@Slf4j
public class ThreadConfig {
    public static final String DATA_SAVE_POOL = "dataSaveThreadPool";

    public static final String DATAX_COLLECT_POOL = "dataxThreadPool";

    /**
     * datax抽取使用
     *
     * @return ThreadPoolTaskExecutor
     */
    @Bean(name = DATAX_COLLECT_POOL)
    public ThreadPoolTaskExecutor dataCollectThreadPool() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        //核心线程数量
        threadPoolTaskExecutor.setCorePoolSize(4);
        //最大线程数量
        threadPoolTaskExecutor.setMaxPoolSize(8);
        //队列中最大任务数
        threadPoolTaskExecutor.setQueueCapacity(10000);
        //线程名称前缀
        threadPoolTaskExecutor.setThreadNamePrefix("dataxThreadPool-");
        //拒绝策略
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //线程空闲后最大存活时间
        threadPoolTaskExecutor.setKeepAliveSeconds(15);
        //初始化线程池
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    /**
     * 数据落库使用
     *
     * @return ThreadPoolTaskExecutor
     */
    @Bean(name = DATA_SAVE_POOL)
    public ThreadPoolTaskExecutor dataSaveThreadPool() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        //核心线程数量
        threadPoolTaskExecutor.setCorePoolSize(4);
        //最大线程数量
        threadPoolTaskExecutor.setMaxPoolSize(8);
        //队列中最大任务数
        threadPoolTaskExecutor.setQueueCapacity(10000);
        //线程名称前缀
        threadPoolTaskExecutor.setThreadNamePrefix("dataSaveThreadPool-");
        //拒绝策略
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //线程空闲后最大存活时间
        threadPoolTaskExecutor.setKeepAliveSeconds(15);
        //初始化线程池
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
