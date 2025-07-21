package com.lrhealth.data.converge.controller;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查
 *
 * @author lr
 */
@Slf4j
@RestController()
@RequestMapping("/health")
public class HealthController {


    private static final String OK = "ok";


    /**
     * 健康检查
     * 用于检测服务启动、运行
     *
     * @return 固定字符串
     */
    @GetMapping(value = "/check")
    public String health() {
        return OK;
    }

    /**
     * 健康检查-心跳检测
     * 用于检测服务启动、运行
     *
     * @return 当前时间
     */
    @GetMapping(value = "/heartbeat")
    public String heartbeat() {
        return DateUtil.now();
    }

}
