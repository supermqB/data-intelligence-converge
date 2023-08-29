package com.lrhealth.data.converge.controller;

import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.FileExecInfoDTO;
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
     * 任务调度中dataX方式创建xds调用
     *
     * @return 固定字符串
     */
    @PostMapping(value = "/create")
    public DolphinSchedulerReturnVO create(@RequestBody TaskDto dto) {
        try {
            FileExecInfoDTO vo = taskService.createTask(dto);
            return new DolphinSchedulerReturnVO("200", vo);
        } catch (Exception e) {
            return new DolphinSchedulerReturnVO("500", e);
        }
    }


    /**
     * 完成汇聚任务
     * 主要用于更新状态
     * 任务调度中dataX方式更新xds调用
     *
     * @return 固定字符串
     */
    @PostMapping(value = "/completed")
    public DolphinSchedulerReturnVO completed(@RequestBody TaskDto taskDto) {
        try {
            Xds xds = taskService.updateTask(taskDto);
            return new DolphinSchedulerReturnVO("200", xds);
        } catch (Exception e) {
            return new DolphinSchedulerReturnVO("500", e);
        }
    }


    /**
     * 前置机文件模式
     *
     * @param projectId 项目ID
     */
    @PostMapping(value = "/fep")
    public void fepFileModel(@RequestParam(value = "projectId") String projectId){
        taskService.fepConverge(projectId);
    }

}
