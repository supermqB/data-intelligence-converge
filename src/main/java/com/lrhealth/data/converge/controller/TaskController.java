package com.lrhealth.data.converge.controller;

import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.DolphinSchedulerReturnVO;
import com.lrhealth.data.converge.model.TaskDto;
import com.lrhealth.data.converge.service.DocumentParseService;
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

    @Resource
    private DocumentParseService documentParseService;

    /**
     * 创建汇聚任务
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
     *
     * @return 固定字符串
     */
    @PostMapping(value = "/completed")
    public DolphinSchedulerReturnVO completed(@RequestBody TaskDto taskDto) {
        Xds xds = taskService.updateTask(taskDto);
        return new DolphinSchedulerReturnVO("200", xds);
    }

    @PostMapping(value = "/fileSave")
    public void fileDocumentAndSave(@RequestParam(value = "id") Long id){
        documentParseService.documentParseAndSave(id);
    }
}
