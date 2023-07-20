package com.lrhealth.data.converge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 汇聚任务处理接口
 * </p>
 *
 * @author lr
 * @since 2023/7/19
 */
@RestController("/task")
public class TaskController {
    /**
     * 创建汇聚任务
     *
     * @return 固定字符串
     */
    @GetMapping(value = "/create")
    public String create() {
        return "";
    }

    /**
     * 完成汇聚任务
     * 主要用于更新状态
     *
     * @return 固定字符串
     */
    @GetMapping(value = "/complated")
    public String complated() {
        return "";
    }
}
