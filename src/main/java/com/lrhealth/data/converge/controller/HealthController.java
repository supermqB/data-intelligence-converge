package com.lrhealth.data.converge.controller;

import cn.hutool.core.date.DateUtil;
import com.lrhealth.data.converge.common.util.ResResult;
import com.lrhealth.data.converge.dao.service.ConvMonitorService;
import com.lrhealth.data.converge.model.dto.MonitorMsg;
import com.lrhealth.data.converge.scheduled.ConvMonitorTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 健康检查
 *
 * @author lr
 */
@Slf4j
@RestController()
@RequestMapping("/health")
public class HealthController {
    @Resource
    private ConvMonitorService convMonitorService;
    @Resource
    private ConvMonitorTask convMonitorTask;

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


    /**
     * 健康检查-心跳检测
     * 用于检测服务启动、运行
     *
     * @return 当前时间
     */
    @PostMapping(value = "/monitor")
    public ResResult<Void> monitor(@RequestBody MonitorMsg monitorMsg) {
        convMonitorService.handleMonitorMsg(monitorMsg);
        return ResResult.success();
    }

}
