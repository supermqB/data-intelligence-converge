package com.lrhealth.data.converge.scheduled.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.scheduled.config.ConvergeConfig;
import com.lrhealth.data.converge.scheduled.dao.entity.*;
import com.lrhealth.data.converge.scheduled.dao.service.*;
import com.lrhealth.data.converge.scheduled.model.FileTask;
import com.lrhealth.data.converge.scheduled.model.TaskFileConfig;
import com.lrhealth.data.converge.scheduled.model.dto.*;
import com.lrhealth.data.converge.scheduled.service.ConvergeService;
import com.lrhealth.data.converge.scheduled.service.FeNodeService;
import com.lrhealth.data.converge.service.DiTaskConvergeService;
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
    private ConvTaskResultFileService convTaskResultFileService;

    @Resource
    private ConvergeConfig convergeConfig;
    @Resource
    private DiTaskConvergeService diTaskConvergeService;

    @Override
    public void updateDownLoadFileTask(ConcurrentLinkedDeque<FileTask> taskDeque) {
        List<ConvTask> list = convTaskService.list(new LambdaQueryWrapper<ConvTask>()
                .eq(ConvTask::getStatus, 3));
        for (ConvTask convTask : list) {
            addTaskResultView(taskDeque, convTask);
            addTaskResultFile(taskDeque, convTask);
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
                log.warn("不存在的管道信息！" + tunnelStatusDto);
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
                updateTaskResultView(taskDeque, taskStatusDto, convTask);
                updateTaskResultFile(taskDeque, taskStatusDto, convTask);
            }
        }
        log.info("前置机：" + node.getIp() + " 状态更新结束！");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFileStatus(TaskFileConfig taskFileConfig, long costTime) {
        try {

            // 通知入库
         //   diTaskConvergeService.dataSave(taskResultView);

            boolean flag = getTaskStatusFlag(taskFileConfig,costTime);
            if (flag) {
                Integer taskId = taskFileConfig.getConvTask().getId();
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

    public boolean getTaskStatusFlag(TaskFileConfig taskFileConfig, long costTime) {
        String convergeMethod = taskFileConfig.getConvTask().getConvergeMethod();

        if ("1".equals(convergeMethod)){
            return updateTaskResultViewStatus(taskFileConfig.getTaskResultView(), costTime);
        }

        if ("3".equals(convergeMethod)){
            return updateTaskResultFileStatus(taskFileConfig.getTaskResultFile(), costTime);
        }
        return false;
    }

    private boolean updateTaskResultFileStatus(ConvTaskResultFile taskResultFile, long costTime) {
        taskResultFile.setTransferTime(costTime);
        taskResultFile.setStatus(3);
        convTaskResultFileService.updateById(taskResultFile);
        List<ConvTaskResultFile> taskResultFiles = convTaskResultFileService.list(new LambdaQueryWrapper<ConvTaskResultFile>()
                .eq(ConvTaskResultFile::getTaskId, taskResultFile.getTaskId())
                .ne(ConvTaskResultFile::getId, taskResultFile.getId()));
        boolean flag = true;
        for (ConvTaskResultFile resultFile : taskResultFiles) {
            if (resultFile.getStatus() == 1 || resultFile.getStatus() == 2) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    private boolean updateTaskResultViewStatus(ConvTaskResultView taskResultView, long costTime) {
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
        return flag;
    }

    @Override
    @Transactional
    public void resetStatus(ConvTask convTask, TaskFileConfig taskFileConfig) {
        convTask.setStatus(3);
        convTaskService.updateById(convTask);
        String convergeMethod = convTask.getConvergeMethod();
        if ("1".equals(convergeMethod)){
            ConvTaskResultView taskResultView = taskFileConfig.getTaskResultView();
            taskResultView.setStatus(1);
            convTaskResultViewService.updateById(taskResultView);
        }
        if("3".equals(convergeMethod)){
            ConvTaskResultFile taskResultFile = taskFileConfig.getTaskResultFile();
            taskResultFile.setStatus(1);
            convTaskResultFileService.updateById(taskResultFile);
        }
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
                .eq(ConvTaskResultView::getFeStoredFilename, fileName),false);
        ConvTaskResultFile taskResultFile = convTaskResultFileService.getOne(new LambdaQueryWrapper<ConvTaskResultFile>()
                .eq(ConvTaskResultFile::getTaskId, taskId)
                .eq(ConvTaskResultFile::getFeStoredFilename, fileName),false);
        String url = feNode.getIp() + ":" + feNode.getPort() + "/file";
        String destPath = convergeConfig.getOutputPath() + File.separator + fileTask.getTaskId()
                + File.separator + fileTask.getFileName().replace(".","_") + File.separator;

        return new TaskFileConfig(convTask, frontNodeTask, tunnel, feNode, taskResultView, taskResultFile,url,
                destPath);
    }

    private void addTaskResultView(ConcurrentLinkedDeque<FileTask> taskDeque, ConvTask convTask) {
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

    private void addTaskResultFile(ConcurrentLinkedDeque<FileTask> taskDeque, ConvTask convTask) {
        List<ConvTaskResultFile> taskResultFiles = convTaskResultFileService.list(new LambdaQueryWrapper<ConvTaskResultFile>()
                .eq(ConvTaskResultFile::getTaskId, convTask.getId())
                .and((l) -> l.eq(ConvTaskResultFile::getStatus,1)
                        .or()
                        .eq(ConvTaskResultFile::getStatus,2)));
        for (ConvTaskResultFile taskResultFile : taskResultFiles) {
            FileTask fileTask = new FileTask(convTask.getId(), taskResultFile.getFeStoredFilename());
            if (!taskDeque.contains(fileTask)) {
                taskDeque.add(fileTask);
            }
        }
    }


    private void updateTaskResultFile(ConcurrentLinkedDeque<FileTask> taskDeque, TaskStatusDto taskStatusDto, ConvTask convTask) {
        List<ResultFileInfoDto> fileInfoList = taskStatusDto.getFileInfoList();
        if (CollUtil.isEmpty(fileInfoList)){
            return;
        }
        for (ResultFileInfoDto resultFileInfoDto : fileInfoList) {
            if (resultFileInfoDto == null) {
                continue;
            }
            //更新 file
            ConvTaskResultFile taskResultFile = feNodeService.saveOrUpdateFile(resultFileInfoDto, convTask);

            //添加任务
            if (convTask.getStatus() == 3 && taskResultFile.getStatus() == 1) {
                FileTask fileTask = new FileTask(convTask.getId(), taskResultFile.getFeStoredFilename());
                if (!taskDeque.contains(fileTask)) {
                    taskDeque.add(fileTask);
                    taskResultFile.setStatus(2);
                    convTaskResultFileService.updateById(taskResultFile);
                    log.info("添加文件下载任务: " + taskResultFile);
                }
            }
        }
    }

    private void updateTaskResultView(ConcurrentLinkedDeque<FileTask> taskDeque, TaskStatusDto taskStatusDto, ConvTask convTask) {
        List<ResultViewInfoDto> fileInfoList = taskStatusDto.getDataxInfoList();
        if (CollUtil.isEmpty(fileInfoList)){
            return;
        }
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
    }
}
