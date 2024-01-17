package com.lrhealth.data.converge.ds.controller;

import com.lrhealth.data.converge.ds.dto.DsResult;
import com.lrhealth.data.converge.ds.feign.DsFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 健康检查
 *
 * @author lr
 * @since 2023-09-07
 */
@RestController("dsHealthController")
@RequestMapping(value = "/ds/health")
@Slf4j
public class HealthController {
    @Resource
    private DsFeignClient dsFeignClient;

    @GetMapping(value = "/check")
    public String health() {
        DsResult dsResult1 = dsFeignClient.createProject("医院16");
        DsResult dsResult2 = dsFeignClient.createFlow(12283215452960L,"医院16-工作流1");
        DsResult dsResult3 = dsFeignClient.releaseFlow(12283215452960L,12283220703904L,"ONLINE");
        DsResult dsResult4 = dsFeignClient.startFlow(12283215452960L,12283220703904L);
        return "ok";
    }

}
