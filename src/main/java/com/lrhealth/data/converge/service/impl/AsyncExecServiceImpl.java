package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import com.lrhealth.data.converge.common.enums.TunnelStatusEnum;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultView;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskResultViewService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.service.AsyncExecService;
import com.lrhealth.data.converge.service.DataXExecService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

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
    private ConvTaskResultViewService resultViewService;
    @Resource
    private ConvTaskService taskService;

    @Override
    @Async
    public void tunnelExec(ConvTunnel tunnel, Integer taskId, Integer execStatus, Integer oldTaskId) {
        if (TunnelCMEnum.LIBRARY_TABLE.getCode().equals(tunnel.getConvergeMethod())) {
            try {
                dataXExecService.run(tunnel.getId(), taskId, execStatus, oldTaskId);
            }catch (InterruptedException e){
                throw new CommonException("线程中断异常");
            }finally {
                tunnelService.updateById(ConvTunnel.builder().id(tunnel.getId()).status(TunnelStatusEnum.SCHEDULING.getValue()).build());
            }
        }
        // 更新文件大小
        updateFileSize(taskId);
        taskService.updateTaskCompleted(tunnel.getId(), taskId);
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
                        .dataSize(taskFile.length()).status(3).build());
                log.info("file: {}, fileSize: {}", taskFile.getName(), taskFile.length());
            } else {
                log.error("文件不存在，resultViewId: {}", jobExecInstance.getId());
            }
        });
    }
}
