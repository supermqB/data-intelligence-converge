package com.lrhealth.data.converge.scheduled.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.enums.TaskStatusEnum;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTask;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultView;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskResultViewService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.scheduled.service.StatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author jinmengyu
 * @date 2023-09-20
 */
@Slf4j
@Service
public class StatusServiceImpl implements StatusService {
    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private ConvTaskService taskService;
    @Resource
    private ConvTaskResultViewService taskResultViewService;


    @Override
    public void updateTaskCompleted(Long tunnelId, Integer taskId) {
        taskService.taskDownloaded(taskId);
        // 没有任务生成，直接把task更改为done
        if (taskResultViewService.list(new LambdaQueryWrapper<ConvTaskResultView>().eq(ConvTaskResultView::getTaskId, taskId)).isEmpty()){
            taskService.updateById(ConvTask.builder().id(taskId).status(TaskStatusEnum.DONE.getValue()).build());
        }
    }

}
