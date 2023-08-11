package com.lrhealth.data.converge.controller;

import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.DolphinSchedulerReturnVO;
import com.lrhealth.data.converge.model.TaskDto;
import com.lrhealth.data.converge.service.TaskService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 汇聚任务处理接口
 * </p>
 *
 * @author lr
 * @since 2023/7/19
 */
@RestController()
@RequestMapping("/task")
public class TaskController {
    @Resource
    private TaskService taskService;

    /**
     * 创建汇聚任务
     * 任务调度中datax方式创建xds调用
     *
     * @return 固定字符串
     */
    @PostMapping(value = "/create")
    public DolphinSchedulerReturnVO create(@RequestBody TaskDto dto) {
        Xds xds = taskService.createTask(dto);
        return new DolphinSchedulerReturnVO("200", xds);
    }


    /**
     * 完成汇聚任务
     * 主要用于更新状态
     * 任务调度中datax方式更新xds调用
     *
     * @return 固定字符串
     */
    @PostMapping(value = "/completed")
    public DolphinSchedulerReturnVO completed(@RequestBody TaskDto taskDto) {
        Xds xds = taskService.updateTask(taskDto);
        return new DolphinSchedulerReturnVO("200", xds);
    }


    /**
     * 本地文件存储
     * 文件解析和前置机存储在同一服务器下
     * 当两者存储的服务器不同时，需要修改shell脚本，目前尚未适配
     * 任务调度中定时触发
     *
     * @param projectId 项目ID
     */
    @PostMapping(value = "/localFile")
    public void localFileParse(@RequestParam(value = "projectId") String projectId){
        taskService.localFileParse(projectId);
    }

}
