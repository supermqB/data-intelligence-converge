package com.lrhealth.data.converge.controller;

import com.lrhealth.data.common.result.ResultBase;
import com.lrhealth.data.converge.dao.service.ConvOdsDatasourceConfigService;
import com.lrhealth.data.converge.model.dto.DataSourceInfoDto;
import com.lrhealth.data.converge.model.dto.DataSourceParamDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
public class FepController {

    @Resource
    private ConvOdsDatasourceConfigService odsDatasourceConfigService;



    @PostMapping("/datasource/list")
    public ResultBase<List<DataSourceInfoDto>> getDataSource(@RequestBody DataSourceParamDto dto){
        try {
            return ResultBase.success(odsDatasourceConfigService.getOrgReaderSource(dto));
        }catch (Exception e){
            log.error("fep get schedule task error, {}", ExceptionUtils.getStackTrace(e));
            return ResultBase.fail(e.getMessage());
        }
    }
}
