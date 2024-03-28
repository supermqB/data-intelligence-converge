package com.lrhealth.data.converge.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/upload")
    public void uploadFile(@RequestBody Map<String,Object> paramMap,@RequestParam("token") String token){
        long start = System.currentTimeMillis();
        try {
            for (String s : paramMap.keySet()) {
                Object o = paramMap.get(s);
                System.out.println(o);
                System.out.println(token);
            }
        } catch (Exception e) {
            log.info("异常信息 {}",e.getMessage());
        }
        long end = System.currentTimeMillis();
    }
}
