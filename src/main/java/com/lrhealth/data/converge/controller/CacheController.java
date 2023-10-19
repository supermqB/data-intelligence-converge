package com.lrhealth.data.converge.controller;

import com.lrhealth.data.common.result.ResultBase;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultView;
import com.lrhealth.data.converge.service.DiTaskConvergeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author jinmengyu
 * @date 2023-10-19
 * 缓存查看与清除
 */
@Slf4j
@RestController()
@RequestMapping("/cache")
public class CacheController {

    @Resource
    private DiTaskConvergeService diTaskConvergeService;

    @GetMapping("/save-map/get")
    public ResultBase<Map<Integer, List<ConvTaskResultView>>> getDataSaveMap(){
        try {
            return ResultBase.success(diTaskConvergeService.getDataSaveMap());
        }catch (Exception e){
            return ResultBase.fail();
        }
    }

    @GetMapping("/save-map/clear")
    public void clearDataSaveMap(){
        diTaskConvergeService.clearDataSaveMap();
    }
}
