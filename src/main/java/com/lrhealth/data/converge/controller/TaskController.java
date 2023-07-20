package com.lrhealth.data.converge.controller;

import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.TaskDto;
import com.lrhealth.data.converge.service.TaskService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;

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
    @Resource
    private TaskService taskService;

    /**
     * 创建汇聚任务
     *
     * @return 固定字符串
     */
    @PostMapping(value = "/create")
    public String create(TaskDto dto) {
        Xds xds = taskService.createTask(dto);
        return xds.getId() + EMPTY;
    }

    /**
     * 完成汇聚任务
     * 主要用于更新状态
     *
     * @return 固定字符串
     */
    @PostMapping(value = "/completed")
    public String completed() {
        return "";
    }
}
