package com.lrhealth.data.converge.controller;

import com.lrhealth.data.common.result.ResultBase;
import com.lrhealth.data.converge.model.dto.DataSourceInfoDto;
import com.lrhealth.data.converge.service.ImportOriginalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author jinmengyu
 * @date 2024-01-17
 */
@Slf4j
@RestController()
@RequestMapping("/config")
public class UtilTestController {

    @Resource
    private ImportOriginalService originalService;

    @GetMapping(value = "/test")
    public ResultBase<Void> testStructure(@RequestBody DataSourceInfoDto dto){
        try {
            originalService.importPlatformDataType(dto);
            return ResultBase.success();
        }catch (Exception e){
            log.error("platform database dataType error: {}", ExceptionUtils.getStackTrace(e));
            return ResultBase.fail();
        }
    }
}
