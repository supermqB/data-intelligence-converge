package com.lrhealth.data.converge.controller;

import com.alibaba.fastjson2.JSON;
import com.lrhealth.data.common.result.ResultBase;
import com.lrhealth.data.converge.dao.service.ConvOdsDatasourceConfigService;
import com.lrhealth.data.converge.model.dto.*;
import com.lrhealth.data.converge.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
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
    private ConvOdsDatasourceConfigService odsDatasourceConfigService;
    @Resource(name = "kafkaTemplate")
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${spring.kafka.topic.original-structure-get}")
    private String originalStructureTopic;


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

    @PostMapping("/datasource/list")
    public ResultBase<List<DataSourceInfoDto>> getDataSource(@RequestBody DataSourceParamDto dto){
        try {
            return ResultBase.success(odsDatasourceConfigService.getOrgReaderSource(dto));
        }catch (Exception e){
            log.error("fep get schedule task error, {}", ExceptionUtils.getStackTrace(e));
            return ResultBase.fail(e.getMessage());
        }
    }

    @PostMapping("/original/structure/kafka")
    public void sendOriginalStructureKafka(@RequestBody DataSourceParamDto dto){
        try {
            kafkaTemplate.send(originalStructureTopic, JSON.toJSONString(dto));
        }catch (Exception e){
            log.error("fep get schedule task error, {}", ExceptionUtils.getStackTrace(e));
        }
    }
}
