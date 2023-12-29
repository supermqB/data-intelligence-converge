package com.lrhealth.data.converge.controller;

import com.alibaba.fastjson.JSON;
import com.lrhealth.data.common.result.ResultBase;
import com.lrhealth.data.converge.model.dto.ActiveFepUploadDto;
import com.lrhealth.data.converge.model.dto.DbXdsMessageDto;
import com.lrhealth.data.converge.model.dto.FepScheduledDto;
import com.lrhealth.data.converge.model.dto.TunnelMessageDTO;
import com.lrhealth.data.converge.service.FeTunnelConfigService;
import com.lrhealth.data.converge.service.ScheduleTaskService;
import com.lrhealth.data.converge.service.XdsInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.*;

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
public class FepController {

    @Resource
    private FeTunnelConfigService feTunnelConfigService;
    @Resource
    private XdsInfoService xdsInfoService;
    @Resource
    private ScheduleTaskService scheduleTaskService;

    @GetMapping("/config")
    public ResultBase<List<TunnelMessageDTO>> getFepTunnelConfig(@RequestParam("ip") String ip,
                                                                 @RequestParam("port") Integer port){
        try {
            return ResultBase.success(feTunnelConfigService.getFepTunnelConfig(ip, port));
        }catch (Exception e){
            log.error("fep get tunnel error, {}", ExceptionUtils.getStackTrace(e));
            return ResultBase.fail(e.getMessage());
        }
    }

    @PostMapping("/upload/log")
    public ResultBase<Void> updateFepStatus(@RequestBody ActiveFepUploadDto activeFepUploadDto){
        try {
            feTunnelConfigService.updateFepStatus(activeFepUploadDto);
            return ResultBase.success();
        }catch (Exception e){
            log.error("get fep status error, {}", ExceptionUtils.getStackTrace(e));
            return ResultBase.fail(e.getMessage());
        }
    }

    @PostMapping("/xds/create")
    public ResultBase<Boolean> createDbXds(@RequestBody DbXdsMessageDto dbXdsMessageDto){
        try {
            log.info("收到前置机xds创建请求：{}", JSON.toJSONString(dbXdsMessageDto));
            return ResultBase.success(xdsInfoService.fepCreateXds(dbXdsMessageDto));
        }catch (Exception e){
            log.error("db-db xds create error, {}", ExceptionUtils.getStackTrace(e));
            return ResultBase.fail(e.getMessage());
        }
    }

    @PostMapping("/xds/update")
    public ResultBase<Boolean> updateDbXds(@RequestBody DbXdsMessageDto dbXdsMessageDto){
        try {
            log.info("收到前置机xds更新请求：{}", JSON.toJSONString(dbXdsMessageDto));
            return ResultBase.success(xdsInfoService.fepUpdateXds(dbXdsMessageDto));
        }catch (Exception e){
            log.error("db-db xds update error, {}", ExceptionUtils.getStackTrace(e));
            return ResultBase.fail(e.getMessage());
        }
    }


    @PostMapping("/task/schedule")
    public ResultBase<List<FepScheduledDto>> getScheduleTask(@RequestParam("ip") String ip,
                                                             @RequestParam("port") Integer port){
        try {
            return ResultBase.success(scheduleTaskService.getCacheTask(ip, port));
        }catch (Exception e){
            log.error("fep get schedule task error, {}", ExceptionUtils.getStackTrace(e));
            return ResultBase.fail(e.getMessage());
        }
    }



}
