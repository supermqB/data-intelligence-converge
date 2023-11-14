package com.lrhealth.data.converge.controller;

import com.lrhealth.data.common.result.ResultBase;
import com.lrhealth.data.converge.scheduled.model.dto.TunnelMessageDTO;
import com.lrhealth.data.converge.scheduled.service.FeTunnelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 前置机主动调用的管道配置接口
 * @author jinmengyu
 * @date 2023-11-14
 */
@Slf4j
@RestController
@RequestMapping("/fep")
public class TunnelConfigController {

    @Resource
    private FeTunnelConfigService feTunnelConfigService;

    @GetMapping("/config")
    public ResultBase<List<TunnelMessageDTO>> getFepTunnelConfig(@RequestParam("ip") String ip,
                                                                @RequestParam("port") Integer port){
        try {
            return ResultBase.success(feTunnelConfigService.getFepTunnelConfig(ip, port));
        }catch (Exception e){
            log.error(ExceptionUtils.getStackTrace(e));
            return ResultBase.fail(e.getMessage());
        }
    }



}
