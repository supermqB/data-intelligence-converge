package com.lrhealth.data.converge.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.util.ResResult;
import com.lrhealth.data.converge.dao.service.ConvMonitorService;
import com.lrhealth.data.converge.model.ConvMonitorConf;
import com.lrhealth.data.converge.model.dto.MonitorMsg;
import com.lrhealth.data.converge.service.ConvMonitorConfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
    private ConvMonitorConfService convMonitorConfService;


    private static final String OK = "ok";

    /**
     * 查询监控配置
     *
     * @param orgCode 机构id
     * @return 监控配置集合
     */
    @PostMapping(value = "/listByOrgCode")
    public List<ConvMonitorConf> listByOrgCode(@RequestParam String orgCode) {
        return convMonitorConfService.list(new LambdaQueryWrapper<ConvMonitorConf>().eq(ConvMonitorConf::getOrgCode, orgCode));
    }


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
