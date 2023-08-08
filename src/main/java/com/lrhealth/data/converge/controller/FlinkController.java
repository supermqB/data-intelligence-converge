package com.lrhealth.data.converge.controller;

import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.FlinkTaskDto;
import com.lrhealth.data.converge.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * flink方式汇聚数据，拥有文件和db两种方式
 * @author jinmengyu
 * @date 2023-08-07
 */
@Slf4j
@RestController()
@RequestMapping("/flink")
public class FlinkController {

    @Resource
    private TaskService taskService;

    @PostMapping(value = "/xds")
    public Xds flinkConverge(@RequestBody FlinkTaskDto flinkTaskDto){
        return taskService.flinkConverge(flinkTaskDto);
    }
}
