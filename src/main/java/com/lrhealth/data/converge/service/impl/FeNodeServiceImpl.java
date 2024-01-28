package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.config.ConvergeConfig;
import com.lrhealth.data.converge.common.exception.FeNodeStatusException;
import com.lrhealth.data.converge.common.exception.PingException;
import com.lrhealth.data.converge.common.util.RsaUtils;
import com.lrhealth.data.converge.dao.entity.*;
import com.lrhealth.data.converge.dao.service.*;
import com.lrhealth.data.converge.model.dto.*;
import com.lrhealth.data.converge.service.FeNodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.lang.System;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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
    private ConvTaskResultCdcService convTaskResultCdcService;

    @Resource
    private ConvTaskResultFileService convTaskResultFileService;

    private static ConcurrentMap<String, Boolean> logIdMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void logInit(){
        List<ConvTaskLog> convTaskLogs = convTaskLogService.list();
        convTaskLogs.forEach(convTaskLog -> logIdMap.put(convTaskLog.getTaskId() + "-" + convTaskLog.getFedLogId(), true));
        log.info("taskLog加载完成");
    }

    @Override
    @Retryable(value = {PingException.class}, backoff = @Backoff(delay = 2000, multiplier = 1.5))
    public boolean ping(String ip, String port) {
        String url = ip + ":" + port + "/health/pong";
        RSA instance = RsaUtils.getInstance(convergeConfig.getPrivateKeyStr());
        String token = "lrhealth:" + System.currentTimeMillis();
        try {
            log.info("ping: " + url);
            String body = HttpRequest.get(url)
                    .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                    .timeout(3000)
                    .execute().body();
            JSONObject jsonObject = JSON.parseObject(body);
            return "200".equals(jsonObject.get("code"));
        } catch (Exception e) {
            throw new PingException("ping 前置机异常！" + url + "\n" +e.getMessage() + "\n"
            + Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    @Recover
    public boolean recover(PingException e) {
        return false;
    }


    @Override
    @Retryable(value = {FeNodeStatusException.class}, backoff = @Backoff(delay = 2000, multiplier = 1.5))
    public FrontendStatusDto getFeNodeStatus(ConvFeNode node) {
        String url = node.getIp() + ":" + node.getPort() + "/task/front/status";
        RSA instance = RsaUtils.getInstance(convergeConfig.getPrivateKeyStr());
        String token = "lrhealth:" + System.currentTimeMillis();
        try {
            log.info("开始请求前置机状态：{}", url);
            String result = HttpRequest.get(url)
                    .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                    .timeout(3000)
                    .execute().body();
            Object value = JSON.parseObject(result).get("value");
            return JSON.parseObject(JSON.toJSONString(value),
                    FrontendStatusDto.class);
        } catch (Exception e) {
            throw new FeNodeStatusException("获取前置机：" + node.getIp() + "状态异常！\n" + e.getMessage()
            + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    @Recover
    public FrontendStatusDto recover(FeNodeStatusException e) {
        return null;
    }

    @Override
    @Transactional
    public ConvTunnel updateTunnel(TunnelStatusDto tunnelStatusDto) {
        Long tunnelId = tunnelStatusDto.getTunnelId();
        ConvTunnel tunnel = convTunnelService.getById(tunnelId);
        if (tunnel == null){
            log.info("不存在的tunnel: " + tunnelId);
            return null;
        }
        convTunnelService.updateById(ConvTunnel.builder().id(tunnelId).status(tunnelStatusDto.getTunnelStatus()).build());
        return tunnel;
    }

    @Override
    @Transactional
    public ConvTask saveOrUpdateTask(TaskStatusDto taskStatusDto, ConvTunnel tunnel, ConvTask oldTask) {
        ConvTask convTask = new ConvTask();
        if (oldTask != null && oldTask.getStatus() > 3) {
            return oldTask;
        }
        BeanUtils.copyProperties(taskStatusDto, convTask);
        if (oldTask != null){
            convTask.setId(oldTask.getId());
        }
        convTask.setFedTaskId(taskStatusDto.getTaskId());
        convTask.setTunnelId(tunnel.getId());
        convTask.setName(tunnel.getName() + "_任务" + taskStatusDto.getTaskId());
        convTask.setSysCode(tunnel.getSysCode());
        convTask.setOrgCode(tunnel.getOrgCode());
        convTask.setConvergeMethod(tunnel.getConvergeMethod());
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        convTask.setStartTime(LocalDateTime.parse(taskStatusDto.getStartTime(), df));
        if (taskStatusDto.getEndTime() != null) {
            convTask.setEndTime(LocalDateTime.parse(taskStatusDto.getEndTime(), df));
        }
        convTask.setDelFlag(0);
        convTaskService.saveOrUpdate(convTask, new LambdaQueryWrapper<ConvTask>()
                .eq(ConvTask::getTunnelId, tunnel.getId())
                .eq(ConvTask::getFedTaskId, taskStatusDto.getTaskId()));
        return convTask;
    }

    @Override
    @Transactional
    public void saveOrUpdateLog(List<TaskLogDto> taskLogs, ConvTask convTask) {
        if (CollUtil.isEmpty(taskLogs)){
            return;
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<TaskLogDto> newLogList = taskLogs.stream().filter(taskLogDto -> !logIdMap.containsKey(convTask.getId() + "-" + taskLogDto.getLogId())).collect(Collectors.toList());
        if (CollUtil.isEmpty(newLogList)){
            return;
        }
        List<ConvTaskLog> convTaskLogList = new ArrayList<>();
        newLogList.forEach(newLogDto -> {
            ConvTaskLog convTaskLog = new ConvTaskLog();
            convTaskLog.setTaskId(convTask.getId());
            convTaskLog.setFedLogId(newLogDto.getLogId());
            convTaskLog.setLogDetail(newLogDto.getLogDetail());
            convTaskLog.setTimestamp(LocalDateTime.parse(newLogDto.getLogTime(), df));
            convTaskLogList.add(convTaskLog);
        });
        boolean insert = convTaskLogService.saveOrUpdateBatch(convTaskLogList);
        if (insert){
            convTaskLogList.forEach(convTaskLog -> logIdMap.put(convTask.getId() + "-" + convTaskLog.getFedLogId(), true));
        }
    }

    @Override
    @Transactional
    public ConvTaskResultView saveOrUpdateFile(ResultViewInfoDto resultViewInfoDto, ConvTask convTask) {
        ConvTaskResultView convTaskResultView = new ConvTaskResultView();
        ConvTaskResultView taskResultView =
                convTaskResultViewService.getOne(new LambdaQueryWrapper<ConvTaskResultView>()
                .eq(ConvTaskResultView::getTaskId, convTask.getId())
                .eq(ConvTaskResultView::getTableName,resultViewInfoDto.getTableName()), false);
        if (taskResultView != null  && taskResultView.getStatus() > 1){
            return taskResultView;
        }
        BeanUtils.copyProperties(resultViewInfoDto, convTaskResultView);
        convTaskResultView.setTaskId(convTask.getId());
        // 过滤条数
        if (null != resultViewInfoDto.getRecordCount() && resultViewInfoDto.getRecordCount() != 0){
            convTaskResultView.setDataItemCount(resultViewInfoDto.getRecordCount());
        }

        convTaskResultView.setFeStoredPath(resultViewInfoDto.getFilePath());
        if(!StringUtils.isEmpty(resultViewInfoDto.getFileName())){
            String destPath = convergeConfig.getOutputPath() + File.separator + convTask.getId()
                    + File.separator + resultViewInfoDto.getFileName().replace(".","_") + File.separator;
            convTaskResultView.setStoredPath(destPath + resultViewInfoDto.getFileName());
            convTaskResultView.setFeStoredFilename(resultViewInfoDto.getFileName());
        }
        if (CharSequenceUtil.isNotBlank(resultViewInfoDto.getStoredTime())){
            convTaskResultView.setStoredTime(LocalDateTime.parse(resultViewInfoDto.getStoredTime()));
        }
        convTaskResultView.setDataSize(resultViewInfoDto.getFileSize());
        convTaskResultView.setDelFlag(0);

        convTaskResultViewService.saveOrUpdate(convTaskResultView,new LambdaQueryWrapper<ConvTaskResultView>()
                .eq(ConvTaskResultView::getTaskId, convTask.getId())
                .eq(ConvTaskResultView::getTableName, resultViewInfoDto.getTableName()));
        return convTaskResultView;
    }

    @Override
    public ConvTaskResultCdc saveOrUpdateFile(ResultCDCInfoDTO cdcInfoDTO, ConvTask convTask) {
        ConvTaskResultCdc convTaskResultCdc = BeanUtil.copyProperties(cdcInfoDTO, ConvTaskResultCdc.class);
        convTaskResultCdc.setTaskId(Long.valueOf(convTask.getId()));
        convTaskResultCdc.setDelFlag(0);

        // @formatter:off
        convTaskResultCdcService.saveOrUpdate(convTaskResultCdc, new LambdaQueryWrapper<ConvTaskResultCdc>()
            .eq(ConvTaskResultCdc::getFlinkJobId, cdcInfoDTO.getFlinkJobId())
            .eq(ConvTaskResultCdc::getTableName, cdcInfoDTO.getTableName()));
        // @formatter:on
        return convTaskResultCdc;
    }

    @Override
    @Transactional
    public ConvTaskResultFile saveOrUpdateFile(ResultFileInfoDto resultFileInfoDto, ConvTask convTask) {
        ConvTaskResultFile convTaskResultFile = new ConvTaskResultFile();
        ConvTaskResultFile taskResultFile = convTaskResultFileService.getOne(new LambdaQueryWrapper<ConvTaskResultFile>()
                .eq(ConvTaskResultFile::getTaskId, convTask.getId())
                .eq(ConvTaskResultFile::getFeStoredFilename,resultFileInfoDto.getFileName()), false);
        if (taskResultFile != null  && taskResultFile.getStatus() > 1){
            return taskResultFile;
        }
        BeanUtils.copyProperties(resultFileInfoDto, convTaskResultFile);
        convTaskResultFile.setTaskId(convTask.getId());
        convTaskResultFile.setFeStoredPath(resultFileInfoDto.getFilePath());
        if(!StringUtils.isEmpty(resultFileInfoDto.getFileName())){
            String destPath = convergeConfig.getOutputPath() + File.separator + convTask.getId()
                    + File.separator + resultFileInfoDto.getFileName().replace(".","_") + File.separator;
            convTaskResultFile.setStoredPath(destPath + resultFileInfoDto.getFileName());
            convTaskResultFile.setFeStoredFilename(resultFileInfoDto.getFileName());
        }
        convTaskResultFile.setDelFlag(0);

        convTaskResultFileService.saveOrUpdate(convTaskResultFile,new LambdaQueryWrapper<ConvTaskResultFile>()
                .eq(ConvTaskResultFile::getTaskId, convTask.getId())
                .eq(ConvTaskResultFile::getFeStoredFilename, resultFileInfoDto.getFileName()));
        return convTaskResultFile;
    }
}
