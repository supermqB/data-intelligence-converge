package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.config.ConvergeConfig;
import com.lrhealth.data.converge.dao.entity.*;
import com.lrhealth.data.converge.dao.service.*;
import com.lrhealth.data.converge.model.FileTask;
import com.lrhealth.data.converge.model.dto.*;
import com.lrhealth.data.converge.service.FeNodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author zhaohui
 * @version 1.0
 */
@Service
@Slf4j
public class FeNodeServiceImpl implements FeNodeService {

    @Resource
    private ConvergeConfig convergeConfig;

    @Resource
    private ConvTunnelService convTunnelService;

    @Resource
    private ConvTaskService convTaskService;

    @Resource
    private ConvTaskLogService convTaskLogService;

    @Resource
    private ConvTaskResultViewService convTaskResultViewService;

    @Resource
    private ConvTaskResultFileService convTaskResultFileService;

    @Resource
    private ConvTaskResultInterfaceService convTaskResultInterfaceService;

    private static ConcurrentMap<String, Boolean> logIdMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void logInit() {
        List<ConvTaskLog> convTaskLogs = convTaskLogService.list();
        convTaskLogs.forEach(convTaskLog -> logIdMap.put(convTaskLog.getTaskId() + "-" + convTaskLog.getFedLogId(), true));
        log.info("taskLog加载完成");
    }
    @Override
    @Transactional
    public ConvTunnel updateTunnel(TunnelStatusKafkaDto tunnelStatusDto) {
        Long tunnelId = tunnelStatusDto.getTunnelId();
        ConvTunnel tunnel = convTunnelService.getById(tunnelId);
        if (tunnel == null) {
            log.info("不存在的tunnel: " + tunnelId);
            return null;
        }
        convTunnelService.updateById(ConvTunnel.builder().id(tunnelId).status(tunnelStatusDto.getTunnelStatus()).build());
        return tunnel;
    }

    @Override
    @Transactional
    public ConvTask saveOrUpdateTask(TaskInfoKafkaDto dto, ConvTunnel tunnel, ConvTask oldTask) {
        ConvTask convTask = new ConvTask();
        if (oldTask != null && oldTask.getStatus() > 3) {
            return oldTask;
        }
        if (oldTask != null) {
            convTask.setId(oldTask.getId());
        }
        convTask.setStatus(dto.getStatus());
        convTask.setFedTaskId(dto.getTaskId());
        if (ObjectUtil.isNotNull(tunnel)) {
            convTask.setTunnelId(tunnel.getId());
            convTask.setName(tunnel.getName() + "_任务" + dto.getTaskId());
            convTask.setSysCode(tunnel.getSysCode());
            convTask.setOrgCode(tunnel.getOrgCode());
            convTask.setConvergeMethod(tunnel.getConvergeMethod());
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (dto.getStartTime() != null) {
            convTask.setStartTime(LocalDateTime.parse(dto.getStartTime(), df));
        }
        if (dto.getEndTime() != null) {
            convTask.setEndTime(LocalDateTime.parse(dto.getEndTime(), df));
        }
        convTask.setDelFlag(0);
        convTaskService.saveOrUpdate(convTask, new LambdaQueryWrapper<ConvTask>()
                .eq(ConvTask::getFedTaskId, dto.getTaskId())
                .eq(ConvTask::getTunnelId, dto.getTunnelId()));
        return convTask;
    }

    @Override
    @Transactional
    public void saveOrUpdateLog(List<TaskLogDto> taskLogs, Integer taskId) {
        if (CollUtil.isEmpty(taskLogs)) {
            return;
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<TaskLogDto> newLogList =
                taskLogs.stream().filter(taskLogDto -> !logIdMap.containsKey(taskId + "-" + taskLogDto.getLogId())).collect(Collectors.toList());
        if (CollUtil.isEmpty(newLogList)) {
            return;
        }
        List<ConvTaskLog> convTaskLogList = new ArrayList<>();
        newLogList.forEach(newLogDto -> {
            ConvTaskLog convTaskLog = new ConvTaskLog();
            convTaskLog.setTaskId(taskId);
            convTaskLog.setFedLogId(newLogDto.getLogId());
            convTaskLog.setLogDetail(newLogDto.getLogDetail());
            convTaskLog.setTimestamp(LocalDateTime.parse(newLogDto.getLogTime(), df));
            convTaskLogList.add(convTaskLog);
        });
        boolean insert = convTaskLogService.saveOrUpdateBatch(convTaskLogList);
        if (insert) {
            convTaskLogList.forEach(convTaskLog -> logIdMap.put(taskId + "-" + convTaskLog.getFedLogId(), true));
        }
    }

    @Override
    public ConvTaskResultView saveOrUpdateFile(ResultViewInfoDto resultViewInfoDto, Integer taskId) {
        ConvTaskResultView convTaskResultView = new ConvTaskResultView();
        ConvTaskResultView taskResultView =
                convTaskResultViewService.getOne(new LambdaQueryWrapper<ConvTaskResultView>()
                        .eq(ConvTaskResultView::getTaskId, taskId)
                        .eq(ConvTaskResultView::getTableName, resultViewInfoDto.getTableName()), false);
        if (taskResultView != null && taskResultView.getStatus() > 1) {
            return taskResultView;
        }
        if (taskResultView != null){
            BeanUtils.copyProperties(taskResultView, convTaskResultView);
        }
        BeanUtils.copyProperties(resultViewInfoDto, convTaskResultView);
        convTaskResultView.setTaskId(taskId);
        // 过滤条数
        if (null != resultViewInfoDto.getRecordCount() && resultViewInfoDto.getRecordCount() != 0) {
            convTaskResultView.setDataItemCount(resultViewInfoDto.getRecordCount());
        }

        convTaskResultView.setFeStoredPath(resultViewInfoDto.getFilePath());
        if (!StringUtils.isEmpty(resultViewInfoDto.getFileName())) {
            String destPath = convergeConfig.getOutputPath() + File.separator + taskId
                    + File.separator + resultViewInfoDto.getFileName().replace(".", "_") + File.separator;
            convTaskResultView.setStoredPath(destPath + resultViewInfoDto.getFileName());
            convTaskResultView.setFeStoredFilename(resultViewInfoDto.getFileName());
        }
        if (CharSequenceUtil.isNotBlank(resultViewInfoDto.getStoredTime())) {
            convTaskResultView.setStoredTime(LocalDateTime.parse(resultViewInfoDto.getStoredTime()));
        }
        convTaskResultView.setDataSize(resultViewInfoDto.getFileSize());
        convTaskResultView.setDelFlag(0);

        convTaskResultViewService.saveOrUpdate(convTaskResultView, new LambdaQueryWrapper<ConvTaskResultView>()
                .eq(ConvTaskResultView::getTaskId, taskId)
                .eq(ConvTaskResultView::getTableName, resultViewInfoDto.getTableName()));
        return convTaskResultView;
    }

    @Override
    @Transactional
    public ConvTaskResultFile saveOrUpdateFile(ResultFileInfoDto resultFileInfoDto, ConvTask convTask) {
        ConvTaskResultFile convTaskResultFile = new ConvTaskResultFile();
        ConvTaskResultFile taskResultFile = convTaskResultFileService.getOne(new LambdaQueryWrapper<ConvTaskResultFile>()
                .eq(ConvTaskResultFile::getTaskId, convTask.getId())
                .eq(ConvTaskResultFile::getFeStoredFilename, resultFileInfoDto.getFileName()), false);
        if (taskResultFile != null && taskResultFile.getStatus() > 1) {
            return taskResultFile;
        }
        BeanUtils.copyProperties(resultFileInfoDto, convTaskResultFile);
        convTaskResultFile.setTaskId(convTask.getId());
        convTaskResultFile.setFeStoredPath(resultFileInfoDto.getFilePath());
        if (!StringUtils.isEmpty(resultFileInfoDto.getFileName())) {
            String destPath = convergeConfig.getOutputPath() + File.separator + convTask.getId()
                    + File.separator + resultFileInfoDto.getFileName().replace(".", "_") + File.separator;
            convTaskResultFile.setStoredPath(destPath + resultFileInfoDto.getFileName());
            convTaskResultFile.setFeStoredFilename(resultFileInfoDto.getFileName());
        }
        convTaskResultFile.setDelFlag(0);

        convTaskResultFileService.saveOrUpdate(convTaskResultFile, new LambdaQueryWrapper<ConvTaskResultFile>()
                .eq(ConvTaskResultFile::getTaskId, convTask.getId())
                .eq(ConvTaskResultFile::getFeStoredFilename, resultFileInfoDto.getFileName()));
        return convTaskResultFile;
    }

    @Override
    public ConvTaskResultInterface saveOrUpdateInterface(ResultInterfaceDTO resultInfoDto, ConvTask convTask) {
        ConvTaskResultInterface convTaskResultInterface = new ConvTaskResultInterface();
        ConvTaskResultInterface taskResult = convTaskResultInterfaceService.getOne(new LambdaQueryWrapper<ConvTaskResultInterface>()
                .eq(ConvTaskResultInterface::getTaskId, convTask.getId()));
        if (taskResult != null && taskResult.getStatus() > 1) {
            return taskResult;
        }
        BeanUtils.copyProperties(resultInfoDto, convTaskResultInterface);
        convTaskResultInterface.setUpdateTime(LocalDateTime.now());
        convTaskResultInterfaceService.saveOrUpdate(convTaskResultInterface, new LambdaQueryWrapper<ConvTaskResultInterface>()
                .eq(ConvTaskResultInterface::getTaskId, convTask.getId()));
        return taskResult;
    }

    @Override
    @Transactional
    public void updateTaskResultView(ConcurrentLinkedDeque<FileTask> taskDeque, List<ResultViewInfoDto> resultViewInfoDtoList, ConvTask convTask) {
        if (CollUtil.isEmpty(resultViewInfoDtoList)) {
            return;
        }
        for (ResultViewInfoDto resultViewInfoDto : resultViewInfoDtoList) {
            if (resultViewInfoDto == null) {
                continue;
            }
            //更新 file
            ConvTaskResultView taskResultView = this.saveOrUpdateFile(resultViewInfoDto, convTask.getId());

            //添加任务
            if (convTask.getStatus() == 2 && taskResultView.getStatus() == 1) {
                if ("null".equals(taskResultView.getFeStoredFilename())
                        || null == taskResultView.getDataItemCount() || taskResultView.getDataItemCount() == 0) {
                    continue;
                }
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

    @Override
    public void updateTaskResultInterface( List<ResultInterfaceDTO> resultViewList, ConvTask convTask) {
        if (CollUtil.isEmpty(resultViewList)) {
            return;
        }
        for (ResultInterfaceDTO resultInfoDto : resultViewList) {
            if (resultInfoDto == null) {
                continue;
            }
            this.saveOrUpdateInterface(resultInfoDto, convTask);
        }
    }


    @Override
    public void updateTaskResultFile(ConcurrentLinkedDeque<FileTask> taskDeque, List<ResultFileInfoDto> resultFileInfoDtoList, ConvTask convTask) {
        if (CollUtil.isEmpty(resultFileInfoDtoList)) {
            return;
        }
        for (ResultFileInfoDto resultFileInfoDto : resultFileInfoDtoList) {
            if (resultFileInfoDto == null) {
                continue;
            }
            //更新 file
            ConvTaskResultFile taskResultFile = this.saveOrUpdateFile(resultFileInfoDto, convTask);

            //添加任务
            if (taskResultFile.getStatus() == 1 && resultFileInfoDto.getFileType() == 1) {
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
}
