package com.lrhealth.data.converge.ds.feign;

import com.lrhealth.data.converge.ds.dto.DsResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "DSFeignClient",url = "${ds.host}")
@Component
public interface DsFeign {

    @GetMapping(value = "/projects/{projectCode}/process-instances/{id}")
    DsResult queryProcessInstanceById(@RequestHeader(name = "token") String token,
                                      @PathVariable long projectCode,
                                      @PathVariable("id") Integer id);

}
