package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.enums.ExecStatusEnum;
import com.lrhealth.data.converge.common.enums.TaskStatusEnum;
import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import com.lrhealth.data.converge.common.enums.TunnelStatusEnum;
import com.lrhealth.data.converge.common.util.thread.AsyncFactory;
import com.lrhealth.data.converge.dao.entity.ConvTask;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.service.ConvTaskService;
import com.lrhealth.data.converge.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.service.AsyncExecService;
import com.lrhealth.data.converge.service.DataXExecService;
import com.lrhealth.data.converge.service.FileTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author jinmengyu
 * @date 2023-11-01
 */
@Slf4j
@Service
public class AsyncExecServiceImpl implements AsyncExecService {

    @Resource
    private DataXExecService dataXExecService;
    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private ConvTaskService taskService;
    @Resource
    private FileTaskService fileTaskService;

    @Override
    @Async
    public void taskExec(ConvTunnel tunnel, Integer taskId) {
        Long tunnelId = tunnel.getId();
        // DIRECT：直接调度和定时任务 | REFRESH 重新调度
        Integer execStatus = taskId == null ? ExecStatusEnum.DIRECT.getValue() : ExecStatusEnum.REFRESH.getValue();
        // 重新调度生成新的task，之前的task有oldTask保存
        Integer oldTaskId = execStatus == 0 ? null : taskId;
        boolean isCdc = TunnelCMEnum.CDC_LOG.getCode().equals(tunnel.getConvergeMethod());
        ConvTask frontendTask = taskService.createTask(tunnel, isCdc);
        taskId = frontendTask.getId();

        TunnelCMEnum convergeMethod = TunnelCMEnum.of(tunnel.getConvergeMethod());
        if (ObjectUtil.isNull(convergeMethod)){
            throw new CommonException("不支持的汇聚方式");
        }

        AsyncFactory.convTaskLog(taskId, "采集任务开始执行!");
        try {
            switch (convergeMethod){
                case FILE_MODE:
                    fileTaskService.run(tunnelId, taskId, ExecStatusEnum.of(execStatus), oldTaskId);
                    break;
                case LIBRARY_TABLE:
                    dataXExecService.run(tunnelId, taskId, execStatus, oldTaskId);
                    break;
                default:
                    log.error("convergeMethod [{}] is not support", convergeMethod);
            }
        }catch (Exception e){
            // 设置任务失败
            taskService.updateTaskStatus(taskId, TaskStatusEnum.FAILED);
            AsyncFactory.convTaskLog(taskId, ExceptionUtils.getStackTrace(e));
            Thread.currentThread().interrupt();
        }finally {
            tunnelService.updateTunnelStatus(tunnelId, TunnelStatusEnum.SCHEDULING);
        }

        taskService.updateTaskCompleted(taskId);
        AsyncFactory.convTaskLog(taskId, "采集任务执行完成!");
    }

}
