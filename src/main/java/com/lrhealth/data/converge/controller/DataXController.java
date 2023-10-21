package com.lrhealth.data.converge.controller;

import com.lrhealth.data.common.result.ResultBase;
import com.lrhealth.data.converge.service.DataXService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author jinmengyu
 * @date 2023-09-01
 */
@Slf4j
@RestController()
@RequestMapping("/datax")
public class DataXController {
    @Resource
    private DataXService dataXService;


    @PostMapping("/exec")
    public ResultBase execJson(@RequestParam("json") String json,
                                 @RequestParam("jsonPath") String jsonPath){
        try {
            dataXService.execDataX(json, jsonPath);
            return ResultBase.success();
        } catch (Exception e) {
            return ResultBase.fail(e.getMessage());
        }
    }
}
