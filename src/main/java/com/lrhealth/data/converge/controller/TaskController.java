package com.lrhealth.data.converge.controller;

import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.DolphinSchedulerReturnVO;
import com.lrhealth.data.converge.model.FepFileInfoVo;
import com.lrhealth.data.converge.model.TaskDto;
import com.lrhealth.data.converge.service.DocumentParseService;
import com.lrhealth.data.converge.service.TaskService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
    public DolphinSchedulerReturnVO fileDocumentAndSave(@RequestParam(value = "id") Long id){
        Xds xds = documentParseService.documentParseAndSave(id);
        return new DolphinSchedulerReturnVO("200", xds);
    }

    @PostMapping(value = "/file")
    public DolphinSchedulerReturnVO fileConverge(@RequestParam(value = "projectId") String projectId){
        List<FepFileInfoVo> fepFileInfoVos = taskService.fileConverge(projectId);
        return new DolphinSchedulerReturnVO("200", fepFileInfoVos);
    }

    @PostMapping(value = "/localFile")
    public void localFileParse(@RequestParam(value = "projectId") String projectId){
        taskService.localFileParse(projectId);
    }

}
