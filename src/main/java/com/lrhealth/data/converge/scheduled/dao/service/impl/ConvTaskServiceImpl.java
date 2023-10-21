package com.lrhealth.data.converge.scheduled.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.common.enums.TaskStatusEnum;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTask;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.dao.mapper.DiConvTaskMapper;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhangguowen-generator
 * @since 2023-09-18
 */
@Service
public class ConvTaskServiceImpl extends ServiceImpl<DiConvTaskMapper, ConvTask> implements ConvTaskService {


    @Override
    public ConvTask createTask(ConvTunnel tunnel, boolean isCdc) {
        ConvTask task = ConvTask.builder()
                .name(tunnel.getName() + "-任务")
                .startTime(LocalDateTime.now())
                .tunnelId(tunnel.getId())
                .status(isCdc ? TaskStatusEnum.STREAMING.getValue() : TaskStatusEnum.PREPARING.getValue())
                .createTime(LocalDateTime.now())
                .delFlag(0)
                .orgCode(tunnel.getOrgCode())
                .sysCode(tunnel.getSysCode())
                .convergeMethod(tunnel.getConvergeMethod())
                .build();
        this.save(task);
        return this.getById(task.getId());
    }

    @Override
    public void updateTaskStatus(Integer taskId, TaskStatusEnum taskStatusEnum) {
        boolean updated = this.updateById(ConvTask.builder().id(taskId).status(taskStatusEnum.getValue()).updateTime(LocalDateTime.now()).build());
        if (!updated){
            log.error("task update fail, taskId: " + taskId);
        }
    }

    @Override
    public void taskWaitingTransfer(Integer taskId) {
        ConvTask task = this.getById(taskId);
        task.setEndTime(LocalDateTime.now());
        task.setStatus(TaskStatusEnum.WAITING_TRANSFER.getValue());
        task.setUpdateTime(LocalDateTime.now());
        this.updateById(task);
    }

    @Override
    public void taskDownloaded(Integer taskId) {
        ConvTask task = this.getById(taskId);
        task.setEndTime(LocalDateTime.now());
        task.setStatus(TaskStatusEnum.DOWNLOADED.getValue());
        task.setUpdateTime(LocalDateTime.now());
        this.updateById(task);
    }

}
