package com.lrhealth.data.converge.scheduled.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.enums.ExecStatusEnum;
import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import com.lrhealth.data.converge.common.enums.TunnelStatusEnum;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTask;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultView;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskResultViewService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.scheduled.model.dto.TunnelMessageDTO;
import com.lrhealth.data.converge.scheduled.service.DataXExecService;
import com.lrhealth.data.converge.scheduled.service.StatusService;
import com.lrhealth.data.converge.scheduled.service.convergeTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

/**
 * @author jinmengyu
 * @date 2023-09-26
 */
@Slf4j
@Service
public class convergeTaskServiceImpl implements convergeTaskService {
    @Resource
    private DataXExecService dataXService;
    @Resource
    private ConvTaskService frontendTaskService;
    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private StatusService statusService;
    @Resource
    private ConvTaskResultViewService resultViewService;
    @Resource
    private SchedulerConfig schedulerConfig;

    @Override
    public void tunnelConfig(TunnelMessageDTO dto) {
        // 创建/更新/删除管道
        ConvTunnel tunnel = tunnelService.getById(dto.getId());
        if (ObjectUtil.isNull(tunnel)){
            schedulerConfig.cancelTriggerTask(String.valueOf(dto.getId()));
            return;
        }
        // datax执行配置
        if (TunnelCMEnum.LIBRARY_TABLE.getCode().equals(tunnel.getConvergeMethod())) {
            if (ObjectUtil.isNull(dto.getJdbcInfoDto().getTableInfoDtoList())){
                return;
            }
            dataXService.dataXConfig(tunnel, dto.getJdbcInfoDto().getTableInfoDtoList());
        }
    }

    @Override
    public void taskExec(Integer taskId, Long tunnelId) {
        ConvTunnel ft = tunnelService.getById(tunnelId);
        // 管道暂停/废弃，不指定任务
        if (ft.getStatus().equals(TunnelStatusEnum.PAUSE.getValue()) || ft.getStatus().equals(TunnelStatusEnum.ABANDON.getValue())) {
            return;
        }
        boolean isCdc = TunnelCMEnum.CDC_LOG.getCode().equals(ft.getConvergeMethod());
        if (TunnelStatusEnum.PROCESSING.getValue().equals(ft.getStatus()) && isCdc) {
            return;
        }

        // 更新tunnel状态为任务执行中
        tunnelService.updateById(ConvTunnel.builder().id(tunnelId).status(TunnelStatusEnum.PROCESSING.getValue()).build());
        // DIRECT：直接调度和定时任务 | REFRESH 重新调度
        Integer execStatus = taskId == null ? ExecStatusEnum.DIRECT.getValue() : ExecStatusEnum.REFRESH.getValue();
        // 重新调度生成新的task，之前的task有oldTask保存
        Integer oldTaskId = execStatus == 0 ? null : taskId;
        ConvTask frontendTask = frontendTaskService.createTask(ft, isCdc);
        taskId = frontendTask.getId();
        if (TunnelCMEnum.LIBRARY_TABLE.getCode().equals(ft.getConvergeMethod())) {
            try {
                dataXService.run(tunnelId, taskId, execStatus, oldTaskId);
            }catch (InterruptedException e){
                throw new CommonException("线程中断异常");
            }finally {
                tunnelService.updateById(ConvTunnel.builder().id(tunnelId).status(TunnelStatusEnum.SCHEDULING.getValue()).build());
            }
        }
        // 更新文件大小
        updateFileSize(taskId);
        statusService.updateTaskCompleted(tunnelId, taskId);
    }


    private void updateFileSize(Integer taskId){
        List<ConvTaskResultView> jobList = resultViewService.list(new LambdaQueryWrapper<ConvTaskResultView>().eq(ConvTaskResultView::getTaskId, taskId));
        if (ObjectUtil.isNull(jobList)){
            return;
        }
        try {
            Thread.sleep(5000);
        }catch (Exception e){
            log.error("(dataxConfig)log error,{}", ExceptionUtils.getStackTrace(e));
            Thread.currentThread().interrupt();
        }
        jobList.forEach(jobExecInstance -> {
            File taskFile = new File(jobExecInstance.getStoredPath());
            if (taskFile.exists() && taskFile.isFile()){
                resultViewService.updateById(ConvTaskResultView.builder().id(jobExecInstance.getId())
                        .dataSize((int) taskFile.length()).status(3).build());
                log.info("file: {}, fileSize: {}", taskFile.getName(), taskFile.length());
            } else {
                log.error("文件不存在，resultViewId: {}", jobExecInstance.getId());
            }
        });
    }

}
