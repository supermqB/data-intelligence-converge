package com.lrhealth.data.converge.scheduled.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.util.file.FileUtils;
import com.lrhealth.data.converge.scheduled.dao.entity.*;
import com.lrhealth.data.converge.scheduled.dao.service.*;
import com.lrhealth.data.converge.scheduled.model.FileTask;
import com.lrhealth.data.converge.scheduled.model.dto.*;
import com.lrhealth.data.converge.scheduled.service.ConvergeService;
import com.lrhealth.data.converge.scheduled.utils.RsaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

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
    private ConvTaskLogService convTaskLogService;

    @Resource
    private ConvTaskResultViewService convTaskResultViewService;

    @Value("${lrhealth.converge.privateKeyStr}")
    private String privateKeyStr;

    @Value("${lrhealth.converge.path}")
    private String path;

    @Resource
    private Executor threadPoolTaskExecutor;

    @Override
    public Boolean ping(String ip, String port) {
        String url = ip + ":" + port + "/health/pong";
        boolean status;
        RSA instance = RsaUtils.getInstance(privateKeyStr);
        String token = "lrhealth:" + System.currentTimeMillis();
        try {
            log.debug("pingReq: " + url);
            HttpResponse response = HttpRequest.get(url)
                    .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                    .timeout(3000)
                    .execute();
            int statusCode = response.getStatus();
            if (statusCode == 200) {
                String body = response.body();
                JSONObject jsonObject = JSONObject.parseObject(body);
                if ("200".equals(jsonObject.get("code"))) {
                    status = true;
                } else {
                    status = false;
                }
            } else {
                status = false;
            }
        } catch (Exception e) {
            status = false;
        }
        log.debug("pingRes: " + status);
        return status;
    }

    @Override
    public void updateDownLoadFileTask(ConcurrentLinkedDeque<FileTask> taskDeque) {
        List<ConvTask> list = convTaskService.list(new LambdaQueryWrapper<ConvTask>()
                .eq(ConvTask::getStatus, 3));
        for (ConvTask convTask : list) {
            List<ConvTaskResultView> taskResultViews = convTaskResultViewService.list(new LambdaQueryWrapper<ConvTaskResultView>()
                    .eq(ConvTaskResultView::getTaskId, convTask.getId()));
            for (ConvTaskResultView taskResultView : taskResultViews) {
                FileTask fileTask = new FileTask(convTask.getId().intValue(), taskResultView.getFeStoredFilename());
                if(!taskDeque.contains(fileTask)){
                    taskDeque.add(fileTask);
                }
            }
        }
    }

    @Override
    @Transactional
    public void updateFeNodeStatus(Long feNodeId, ConcurrentLinkedDeque<FileTask> taskDeque) {

        RSA instance = RsaUtils.getInstance(privateKeyStr);
        String token = "lrhealth:" + System.currentTimeMillis();

        ConvFeNode node = convFeNodeService.getById(feNodeId);
        if (node.getState() == 0){
            return;
        }
        String url = node.getIp() + ":" + node.getPort() + "/task/front/status";
        if (!this.ping(node.getIp(), String.valueOf(node.getPort()))){
            node.setState(0);
            convFeNodeService.updateById(node);
            log.error("前置机：" + node.getIp() + "ping 异常！");
            return;
        }

        String result;
        try {
            log.debug("statusReq: " + url);
            result = HttpRequest.get(url)
                    .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                    .timeout(3000)
                    .execute().body();
        } catch (Exception e) {
            log.error("获取前置机：" + node.getIp() + "状态异常！");
            return;
        }
        log.debug("statusRes: " + result);
        FrontendStatusDto frontendStatusDto = JSONObject.parseObject(result, FrontendStatusDto.class);
        List<TunnelStatusDto> tunnelStatusDtoList = frontendStatusDto.getTunnelStatusDtoList();

        for (TunnelStatusDto tunnelStatusDto : tunnelStatusDtoList) {
            Long tunnelId = tunnelStatusDto.getTunnelId();
            ConvTunnel tunnel = convTunnelService.getById(tunnelId);
            tunnel.setStatus(tunnelStatusDto.getTunnelStatus());
            List<TaskStatusDto> taskStatusList = tunnelStatusDto.getTaskStatusList();
            for (TaskStatusDto taskStatusDto : taskStatusList) {
                ConvTask convTask = new ConvTask();
                BeanUtils.copyProperties(taskStatusDto,convTask);
                convTask.setId(taskStatusDto.getTaskId());
                convTask.setCreateTime(LocalDateTime.parse(taskStatusDto.getStartTime()));
                convTask.setEndTime(LocalDateTime.parse(taskStatusDto.getEndTime()));
                convTaskService.saveOrUpdate(convTask);

                List<String> taskLogs = taskStatusDto.getTaskLogs();
                for (String taskLog : taskLogs) {
                    ConvTaskLog convTaskLog = new ConvTaskLog();
                    convTaskLog.setTaskId(convTask.getId());
                    convTaskLog.setLogDetail(taskLog);
                    convTaskLogService.saveOrUpdate(convTaskLog);
                }

                List<ResultViewInfoDto> fileInfoList = taskStatusDto.getFileInfoList();

                for (ResultViewInfoDto resultViewInfoDto : fileInfoList) {
                    ConvTaskResultView convTaskResultView = new ConvTaskResultView();
                    BeanUtils.copyProperties(resultViewInfoDto,convTaskResultView);
                    convTaskResultView.setTaskId(convTask.getId().intValue());
                    convTaskResultView.setDataItemCount(resultViewInfoDto.getRecordCount());
                    convTaskResultView.setFeStoredPath(resultViewInfoDto.getFilePath());
                    convTaskResultView.setStoredPath(path + File.separator + resultViewInfoDto.getFileName());
                    convTaskResultView.setFeStoredFilename(resultViewInfoDto.getFileName());
                    convTaskResultViewService.saveOrUpdate(convTaskResultView);

                    if(taskStatusDto.getStatus() == 3){
                        FileTask fileTask = new FileTask(taskStatusDto.getTaskId().intValue(), resultViewInfoDto.getFileName());
                        if(!taskDeque.contains(fileTask)){
                            taskDeque.add(fileTask);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String prepareFiles(String url, FileTask fileTask) {
        RSA instance = RsaUtils.getInstance(privateKeyStr);
        String token = "lrhealth:" + System.currentTimeMillis();
        return HttpRequest.post(url + "/prepareFiles")
                .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                .body(JSONObject.toJSONString(fileTask))
                .timeout(3000).execute().body();

    }

    @Override
    public PreFileStatusDto getPreFilesStatus(String url, FileTask fileTask) {
        RSA instance = RsaUtils.getInstance(privateKeyStr);
        String token = "lrhealth:" + System.currentTimeMillis();
        String statusResponse = HttpRequest.post(url + "/prepareFiles/status" )
                .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                .body(JSONObject.toJSONString(fileTask))
                .timeout(3000).execute().body();
        return JSONObject.parseObject(statusResponse, PreFileStatusDto.class);
    }

    @Override
    public void downLoadFile(String url, PreFileStatusDto preFileStatusDto) {
        List<CompletableFuture<String>> futureList = new ArrayList<>();
        RSA instance = RsaUtils.getInstance(privateKeyStr);
        String token = "lrhealth:" + System.currentTimeMillis();
        for (Map.Entry<String, Integer> entry : preFileStatusDto.getPartFileMap().entrySet()) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                int i = 0;
                HttpResponse execute = HttpRequest.get(url + "/downloadFiles/" + entry.getKey())
                        .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                        .executeAsync();
                long l = execute.writeBody(path + File.separator + entry.getKey());
                while (l != entry.getValue() && i < 3) {
                    execute = HttpRequest.get(url + "/downloadFiles/" + entry.getKey())
                            .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                            .executeAsync();
                    l = execute.writeBody(path + File.separator + entry.getKey());
                    i++;
                }
                if(i == 3){
                    log.error("文件传输重试超过3次！");
                    return entry.getKey();
                }
                return "";
            }, threadPoolTaskExecutor);
            futureList.add(future);
        }
        //同步结果-校验-重试
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                .thenApply(e -> futureList.stream().map(CompletableFuture::join)
                        .collect(Collectors.toList())).join();
    }

    @Override
    public void deleteFiles(String url, FileTask fileTask) {
        RSA instance = RsaUtils.getInstance(privateKeyStr);
        String token = "lrhealth:" + System.currentTimeMillis();
        //通知删除
        HttpRequest.post(url + "/deleteFiles")
                .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                .body(JSONObject.toJSONString(fileTask))
                .timeout(3000).execute().body();
    }

    @Override
    @Transactional
    public void updateFileStatus(ConvTaskResultView taskResultView) {
        taskResultView.setStatus(4);
        convTaskResultViewService.updateById(taskResultView);
    }
}
