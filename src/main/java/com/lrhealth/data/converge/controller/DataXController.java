package com.lrhealth.data.converge.controller;

import com.lrhealth.data.common.result.ResultBase;
import com.lrhealth.data.converge.model.DataBaseMessageDTO;
import com.lrhealth.data.converge.service.DataXService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    @PostMapping("/create")
    public ResultBase<List<String>> createDataXJson(@RequestBody DataBaseMessageDTO dto,
                                                    @RequestParam("filePath") String oriFilePath){
        try {
            return ResultBase.success(dataXService.createJson(dto, oriFilePath));
        } catch (Exception e) {
            return ResultBase.fail(e.getMessage());
        }
    }

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
