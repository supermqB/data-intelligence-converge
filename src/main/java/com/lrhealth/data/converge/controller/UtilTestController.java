package com.lrhealth.data.converge.controller;

import com.lrhealth.data.converge.dao.service.XdsService;
import com.lrhealth.data.converge.service.IncrTimeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author jinmengyu
 * @date 2024-01-17
 */
@RestController()
@RequestMapping("/test")
public class UtilTestController {
    @Resource
    private IncrTimeService incrTimeService;
    @Resource
    private XdsService xdsService;
    @GetMapping(value = "/time")
    public void updateTime(){
        incrTimeService.updateTableLatestTime(1747433178191933440L);
    }
}
