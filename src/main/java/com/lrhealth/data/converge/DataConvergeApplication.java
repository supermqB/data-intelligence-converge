package com.lrhealth.data.converge;

import cn.hutool.core.exceptions.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.net.InetAddress;

/**
 * 数据汇聚启动类
 *
 * @author lr
 */
@SpringBootApplication(scanBasePackages = {"com.lrhealth.data.*"})
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAsync
@EnableRetry
@EnableFeignClients
public class DataConvergeApplication {
    private static final Logger log = LoggerFactory.getLogger(DataConvergeApplication.class);

    public static void main(String[] args) {
        log.info("********** DataConvergeApplication start ! **********");

        try {
            //设置jasypt密码
            System.setProperty("jasypt.encryptor.password", "PEB123@321BEP");
            System.setProperty("converge.ip", InetAddress.getLocalHost().getHostAddress());
            SpringApplication.run(DataConvergeApplication.class, args);
        } catch (Exception e) {
            log.error("********** DataConvergeApplication error:{}", ExceptionUtil.stacktraceToString(e));
        }
        log.info("********** DataConvergeApplication completed ! **********");
    }

}
