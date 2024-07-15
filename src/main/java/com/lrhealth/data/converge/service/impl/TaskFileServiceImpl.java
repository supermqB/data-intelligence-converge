package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import com.lrhealth.data.converge.common.config.ConvergeConfig;
import com.lrhealth.data.converge.common.exception.*;
import com.lrhealth.data.converge.common.util.RsaUtils;
import com.lrhealth.data.converge.common.util.file.FileUtils;
import com.lrhealth.data.converge.model.FileTask;
import com.lrhealth.data.converge.model.TaskFileConfig;
import com.lrhealth.data.converge.model.dto.PreFileStatusDto;
import com.lrhealth.data.converge.service.TaskFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author zhaohui
 * @version 1.0
 */
@Service
@Slf4j
public class TaskFileServiceImpl implements TaskFileService {

    @Resource
    private ConvergeConfig convergeConfig;
    @Resource
    private Executor threadPoolTaskExecutor;

    @Override
    @Retryable(value = {FileSplitException.class}, backoff = @Backoff(delay = 2000, multiplier = 1.5))
    public boolean splitFile(TaskFileConfig taskFileConfig) {
        RSA instance = RsaUtils.getInstance(convergeConfig.getPrivateKeyStr());
        String token = "lrhealth:" + System.currentTimeMillis();
        String result;
        try {
            HttpRequest httpRequest = HttpRequest.post(taskFileConfig.getUrl() + "/prepareFiles")
                    .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                    .body(JSONObject.toJSONString(taskFileConfig.getFrontNodeTask()))
                    .timeout(3000);
            result = httpRequest.execute().body();
            if (!"true".equals(result)) {
                throw new FileSplitException("通知文件拆分异常！\n" + result);
            }
            return true;
        } catch (Exception e) {
            throw new FileSplitException("通知文件拆分异常！\n" + e.getMessage() + "\n" +
                    Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    @Recover
    public boolean recoverSplit(FileSplitException e) {
        return false;
    }

    @Override
    @Retryable(value = {FileStatusException.class}, maxAttempts = 12, backoff = @Backoff(delay = 2000, multiplier =
            1.5))
    public PreFileStatusDto getFileStatus(TaskFileConfig taskFileConfig) {
        RSA instance = RsaUtils.getInstance(convergeConfig.getPrivateKeyStr());
        String token = "lrhealth:" + System.currentTimeMillis();
        try {
            String statusResponse = HttpRequest.post(taskFileConfig.getUrl() + "/prepareFiles/status")
                    .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                    .body(JSONObject.toJSONString(taskFileConfig.getFrontNodeTask()))
                    .timeout(3000).execute().body();
            PreFileStatusDto preFileStatusDto = JSONObject.parseObject(statusResponse, PreFileStatusDto.class);
            if(!"1".equals(preFileStatusDto.getStatus())){
                throw new FileStatusException("正在轮询文件拆分状态...");
            }
            return preFileStatusDto;
        } catch (Exception e) {
            throw new FileStatusException("查询文件拆分状态异常！\n" + e.getMessage() + "\n" +
                    Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    @Recover
    public PreFileStatusDto recoverStatus(FileStatusException e) {
        return null;
    }

    @Override
    @Retryable(value = {FileDownloadException.class}, backoff = @Backoff(delay = 2000, multiplier = 1.5))
    public boolean downloadFile(PreFileStatusDto preFileStatusDto, TaskFileConfig taskFileConfig) {
        List<CompletableFuture<String>> futureList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : preFileStatusDto.getPartFileMap().entrySet()) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                int i = 0;
                long l = writeFile(taskFileConfig, entry);
                while (l != entry.getValue() && i < 3) {
                    l = writeFile(taskFileConfig, entry);
                    i++;
                }
                if (i == 3) {
                    log.error("文件传输重试超过3次！" + entry.getKey());
                    return entry.getKey();
                }
                return "";
            }, threadPoolTaskExecutor).exceptionally(e -> {
                throw new FileDownloadException("文件下载异常！" + entry.getKey() + "\n"
                        + e.getMessage() + "\n"
                        + Arrays.toString(e.getStackTrace()));
            });
            futureList.add(future);
        }
        //同步结果-校验-重试
        List<String> join = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                .thenApply(e -> futureList.stream().map(CompletableFuture::join)
                        .collect(Collectors.toList())).join();
        return StringUtils.isEmpty(String.join("", join));
    }

    @Override
    @Recover
    public boolean recoverDownload(FileDownloadException e) {
        return false;
    }

    @Override
    @Retryable(value = {FileMergeException.class}, backoff = @Backoff(delay = 2000, multiplier = 1.5))
    public boolean mergeFile(TaskFileConfig taskFileConfig) {
        FileUtils fileUtil = new FileUtils();
        try {
            fileUtil.mergePartFiles(threadPoolTaskExecutor,
                    taskFileConfig.getDestPath(),
                    ".part",
                    taskFileConfig.getTunnel().getDataShardSize().intValue(),
                    taskFileConfig.getDestPath() + File.separator
                            + taskFileConfig.getFrontNodeTask().getFileName(),
                    Base64Decoder.decode(taskFileConfig.getFeNode().getAesKey()));
            return true;
        } catch (Exception e) {
            throw new FileMergeException(e.getMessage());
        }
    }

    @Override
    @Recover
    public boolean recoverMerge(FileMergeException e) {
        return false;
    }

    @Override
    @Retryable(value = {FileDeleteException.class}, backoff = @Backoff(delay = 2000, multiplier = 1.5))
    public boolean deleteFile(TaskFileConfig taskFileConfig) {
        RSA instance = RsaUtils.getInstance(convergeConfig.getPrivateKeyStr());
        String token = "lrhealth:" + System.currentTimeMillis();
        try {
            String result = HttpRequest.post(taskFileConfig.getUrl() + "/deleteFiles")
                    .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                    .body(JSONObject.toJSONString(taskFileConfig.getFrontNodeTask()))
                    .timeout(3000).execute().body();
            if (!"true".equals(result)) {
                throw new FileSplitException("通知文件删除异常！\n" + result);
            }
            return true;
        } catch (Exception e) {
            throw new FileDeleteException("文件删除异常！" + taskFileConfig.getFrontNodeTask().getFileName() + "\n"
                    + e.getMessage() + "\n"
                    + Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    @Recover
    public boolean recoverDelete(FileDeleteException e) {
        return false;
    }

    private long writeFile(TaskFileConfig taskFileConfig, Map.Entry<String, Integer> entry) {
        RSA instance = RsaUtils.getInstance(convergeConfig.getPrivateKeyStr());
        String token = "lrhealth:" + System.currentTimeMillis();
        FileTask fileTask = taskFileConfig.getFrontNodeTask();
        HttpResponse execute = HttpRequest.get(taskFileConfig.getUrl() + "/downloadFiles")
                .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                .form("taskId", fileTask.getTaskId())
                .form("fileName", fileTask.getFileName())
                .form("partFileName", entry.getKey())
                .executeAsync();
        return execute.writeBody(taskFileConfig.getDestPath());
    }

}
