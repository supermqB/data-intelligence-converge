package com.lrhealth.data.converge.controller;

import com.lrhealth.data.converge.common.util.TokenUtil;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.service.ConvTaskService;
import com.lrhealth.data.converge.service.ApiTransService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 接口传输
 * @author zhuanning
 * @date 2024-07-27
 */
@Slf4j
@RestController()
@RequestMapping("/api/transmission")
public class ApiTransController {

    @Resource
    private ApiTransService apiTransService;
    @Resource
    private ConvTaskService convTaskService;

    @PostMapping("/upload")
    public void upload(@RequestBody Map<String,Object> paramMap,@RequestParam("token") String token){
        if (!TokenUtil.validateToken(token)){
            return;
        }
        String tunnelId = TokenUtil.parseJwtSubject(token);
        try {
            ConvTunnel upload = apiTransService.upload(tunnelId, paramMap);
            if (upload != null){
                convTaskService.createTask(upload,false);
            }
        } catch (Exception e) {
            log.info("异常信息 {}",e.getMessage());
        }
    }
}
