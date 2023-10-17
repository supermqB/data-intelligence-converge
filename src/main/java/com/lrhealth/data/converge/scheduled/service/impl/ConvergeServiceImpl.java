package com.lrhealth.data.converge.scheduled.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.scheduled.config.ConvergeConfig;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvFeNode;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTask;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultView;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.dao.service.ConvFeNodeService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskResultViewService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.scheduled.model.FileTask;
import com.lrhealth.data.converge.scheduled.model.TaskFileConfig;
import com.lrhealth.data.converge.scheduled.model.dto.FrontendStatusDto;
import com.lrhealth.data.converge.scheduled.model.dto.ResultCDCInfoDTO;
import com.lrhealth.data.converge.scheduled.model.dto.ResultViewInfoDto;
import com.lrhealth.data.converge.scheduled.model.dto.TaskLogDto;
import com.lrhealth.data.converge.scheduled.model.dto.TaskStatusDto;
import com.lrhealth.data.converge.scheduled.model.dto.TunnelStatusDto;
import com.lrhealth.data.converge.scheduled.service.ConvergeService;
import com.lrhealth.data.converge.scheduled.service.FeNodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author zhaohui
 * @version 1.0
 */
@Service
@Slf4j
public class ConvergeServiceImpl implements ConvergeService {

    @Resource
    private ConvFeNodeService convFeNodeService;

    @Resource
    private ConvTunnelService convTunnelService;

    @Resource
    private ConvTaskService convTaskService;

    @Resource
    private FeNodeService feNodeService;

    @Resource
    private ConvTaskResultViewService convTaskResultViewService;

    @Resource
    private ConvergeConfig convergeConfig;

    @Override
    public void updateDownLoadFileTask(ConcurrentLinkedDeque<FileTask> taskDeque) {
        List<ConvTask> list = convTaskService.list(new LambdaQueryWrapper<ConvTask>()
                .eq(ConvTask::getStatus, 3));
        for (ConvTask convTask : list) {
            List<ConvTaskResultView> taskResultViews = convTaskResultViewService.list(new LambdaQueryWrapper<ConvTaskResultView>()
                    .eq(ConvTaskResultView::getTaskId, convTask.getId())
                    .and((l) -> l.eq(ConvTaskResultView::getStatus,1)
                            .or()
                            .eq(ConvTaskResultView::getStatus,2)));
            for (ConvTaskResultView taskResultView : taskResultViews) {
                FileTask fileTask = new FileTask(convTask.getId(), taskResultView.getFeStoredFilename());
                if (!taskDeque.contains(fileTask)) {
                    taskDeque.add(fileTask);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFeNodeStatus(Long feNodeId, ConcurrentLinkedDeque<FileTask> taskDeque) {
        ConvFeNode node = convFeNodeService.getById(feNodeId);
        if (!feNodeService.ping(node.getIp(), String.valueOf(node.getPort()))) {
            node.setState(0);
            convFeNodeService.updateById(node);
            log.error("前置机：" + node.getIp() + "ping 异常！");
            return;
        }
        //更新前置机节点状态-上线
        node.setState(1);
        convFeNodeService.updateById(node);

        FrontendStatusDto frontendStatusDto = feNodeService.getFeNodeStatus(node);
        if (frontendStatusDto == null || frontendStatusDto.getTunnelStatusDtoList() == null) {
            log.error("status返回结果异常: " + frontendStatusDto);
            return;
        }

        List<TunnelStatusDto> tunnelStatusDtoList = frontendStatusDto.getTunnelStatusDtoList();
        for (TunnelStatusDto tunnelStatusDto : tunnelStatusDtoList) {

            //更新 tunnel
            ConvTunnel tunnel = feNodeService.updateTunnel(tunnelStatusDto);
            if(tunnel == null){
                log.error("管道信息更新失败！" + tunnelStatusDto);
                continue;
            }

            List<TaskStatusDto> taskStatusList = tunnelStatusDto.getTaskStatusList();
            for (TaskStatusDto taskStatusDto : taskStatusList) {
                //更新 task
                ConvTask convTask = feNodeService.saveOrUpdateTask(taskStatusDto,tunnel);
                if (StrUtil.equals(convTask.getConvergeMethod(), "1") && convTask.getStatus() > 3) {
//                    log.info("当前任务已完成，无需更新！" + convTask);
                    continue;
                }

                List<TaskLogDto> taskLogs = taskStatusDto.getTaskLogs();
                for (TaskLogDto taskLog : taskLogs) {
                    //更新 log
                    feNodeService.saveOrUpdateLog(taskLog, convTask);
                }

                List<ResultViewInfoDto> fileInfoList = taskStatusDto.getFileInfoList();
                for (ResultViewInfoDto resultViewInfoDto : fileInfoList) {
                    if (resultViewInfoDto == null) {
                        continue;
                    }
                    //更新 file
                    ConvTaskResultView taskResultView = feNodeService.saveOrUpdateFile(resultViewInfoDto, convTask);

                    //添加任务
                    if (convTask.getStatus() == 3 && taskResultView.getStatus() == 1) {
                        FileTask fileTask = new FileTask(convTask.getId(), taskResultView.getFeStoredFilename());
                        if (!taskDeque.contains(fileTask)) {
                            taskDeque.add(fileTask);
                            taskResultView.setStatus(2);
                            convTaskResultViewService.updateById(taskResultView);
                            log.info("添加文件下载任务: " + taskResultView);
                        }
                    }
                }

                List<ResultCDCInfoDTO> cdcInfoList = taskStatusDto.getCdcInfoList();
                for (ResultCDCInfoDTO resultCDCInfoDTO : cdcInfoList) {
                    feNodeService.saveOrUpdateFile(resultCDCInfoDTO, convTask);
                }
            }
        }
        log.info("前置机：" + node.getIp() + " 状态更新结束！");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFileStatus(TaskFileConfig taskFileConfig, long costTime) {
        try {
            ConvTaskResultView taskResultView = taskFileConfig.getTaskResultView();
            taskResultView.setTransferTime(costTime);
            taskResultView.setStatus(3);
            convTaskResultViewService.updateById(taskResultView);
            List<ConvTaskResultView> taskResultViews = convTaskResultViewService.list(new LambdaQueryWrapper<ConvTaskResultView>()
                    .eq(ConvTaskResultView::getTaskId, taskResultView.getTaskId())
                    .ne(ConvTaskResultView::getId, taskResultView.getId()));
            boolean flag = true;
            for (ConvTaskResultView resultView : taskResultViews) {
                if (resultView.getStatus() == 1 || resultView.getStatus() == 2) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                Integer taskId = taskResultView.getTaskId();
                ConvTask convTask = convTaskService.getById(taskId);
                convTask.setStatus(4);
                convTaskService.updateById(convTask);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            // 手动回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public void resetStatus(ConvTask convTask, ConvTaskResultView taskResultView) {
        convTask.setStatus(3);
        taskResultView.setStatus(1);
        convTaskService.updateById(convTask);
        convTaskResultViewService.updateById(taskResultView);
    }

    public TaskFileConfig getTaskConfig(FileTask fileTask) {
        int taskId = fileTask.getTaskId();
        String fileName = fileTask.getFileName();
        ConvTask convTask = convTaskService.getById(taskId);
        FileTask frontNodeTask = new FileTask(convTask.getFedTaskId(), fileName);
        ConvTunnel tunnel = convTunnelService.getById(convTask.getTunnelId());
        ConvFeNode feNode = convFeNodeService.getById(tunnel.getFrontendId());
        ConvTaskResultView taskResultView = convTaskResultViewService.getOne(new LambdaQueryWrapper<ConvTaskResultView>()
                .eq(ConvTaskResultView::getTaskId, taskId)
                .eq(ConvTaskResultView::getFeStoredFilename, fileName));
        String url = feNode.getIp() + ":" + feNode.getPort() + "/file";
        String destPath = convergeConfig.getOutputPath() + File.separator + fileTask.getTaskId()
                + File.separator + fileTask.getFileName().replace(".","_") + File.separator;

        return new TaskFileConfig(convTask, frontNodeTask, tunnel, feNode, taskResultView, url, destPath);
    }
}
