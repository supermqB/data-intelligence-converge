package com.lrhealth.data.converge.scheduled.service.impl;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import com.lrhealth.data.common.enums.conv.XdsStatusEnum;
import com.lrhealth.data.converge.common.util.file.FileUtils;
import com.lrhealth.data.converge.common.util.file.LargeFileUtil;
import com.lrhealth.data.converge.dao.adpter.JDBCRepository;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.dao.service.XdsService;
import com.lrhealth.data.converge.scheduled.config.ConvergeConfig;
import com.lrhealth.data.converge.scheduled.config.exception.*;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTask;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultView;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskResultViewService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskService;
import com.lrhealth.data.converge.scheduled.model.FileTask;
import com.lrhealth.data.converge.scheduled.model.TaskFileConfig;
import com.lrhealth.data.converge.scheduled.model.dto.PreFileStatusDto;
import com.lrhealth.data.converge.scheduled.service.TaskFileService;
import com.lrhealth.data.converge.scheduled.utils.RsaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    @Resource
    private ConvTaskResultViewService taskResultViewService;
    @Resource
    private XdsService xdsService;
    @Resource
    private ConvTaskService taskService;
    @Resource
    private JDBCRepository jdbcRepository;

    @Override
    @Retryable(value = {FileSplitException.class}, backoff = @Backoff(delay = 2000, multiplier = 1.5))
    public boolean splitFile(TaskFileConfig taskFileConfig) {
        RSA instance = RsaUtils.getInstance(convergeConfig.getPrivateKeyStr());
        String token = "lrhealth:" + System.currentTimeMillis();
        String result;
        try {
            result = HttpRequest.post(taskFileConfig.getUrl() + "/prepareFiles")
                    .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                    .body(JSONObject.toJSONString(taskFileConfig.getFrontNodeTask()))
                    .timeout(3000).execute().body();
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
    @Retryable(value = {FileStatusException.class}, maxAttempts = 20, backoff = @Backoff(delay = 2000, multiplier =
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

    @Override
    @Async
    public void fileParseAndSave(Integer taskResultViewId) {
        // 创建xds
        Xds xds = createXds(taskResultViewId);
        Integer countNumber = LargeFileUtil.csvParseAndInsert(xds.getStoredFilePath(), xds.getStoredFileName(), xds.getId(), xds.getOdsTableName());
        // 获得数据的大概存储大小
        String avgRowLength = getAvgRowLength(xds.getOdsTableName());
        // 更新xds
        updateXds(xds.getId(), countNumber * Long.parseLong(avgRowLength));
        // 发送kafka
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

    private Xds createXds(Integer taskResultViewId){
        ConvTaskResultView taskResultView = taskResultViewService.getById(taskResultViewId);
        ConvTask convTask = taskService.getById(taskResultView.getTaskId());
        Xds xds =  Xds.builder()
                .id(IdUtil.getSnowflakeNextId())
                .orgCode(convTask.getOrgCode())
                .sysCode(convTask.getSysCode())
                .convergeMethod(convTask.getConvergeMethod())
                .dataConvergeStartTime(convTask.getStartTime())
                .dataConvergeStatus(XdsStatusEnum.INIT.getCode())
                .odsModelName(taskResultView.getTableName())
                .oriFileName(taskResultView.getFeStoredFilename())
                .storedFilePath(taskResultView.getStoredPath())
                .storedFileName(taskResultView.getFeStoredFilename())
                .storedFileType("csv")
                .storedFileMode(0)
                .odsTableName(convTask.getSysCode() + "_" + taskResultView.getTableName())
                .storedFileSize(BigDecimal.valueOf(taskResultView.getDataSize()))
                .dataCount(taskResultView.getDataItemCount())
                .createTime(LocalDateTime.now())
                .build();
        xdsService.save(xds);
        return xdsService.getById(xds.getId());
    }

    private void updateXds(Long xdsId, Long dataSize){
        Xds updateXds = Xds.builder().id(xdsId).dataSize(dataSize)
                .dataConvergeEndTime(LocalDateTime.now()).updateTime(LocalDateTime.now()).build();
        xdsService.updateById(updateXds);
    }

    private String getAvgRowLength(String odsTableName){
        // 刷新tables表的数据
        String refreshSql = "ANALYZE TABLE " + odsTableName + " COMPUTE STATISTICS FOR ALL COLUMNS SIZE AUTO;";
        jdbcRepository.execSql(refreshSql);
        // 获取每行的平均大小
        String selectSql = "select AVG_ROW_LENGTH from information_schema.TABLES where TABLE_NAME = '" + odsTableName + "';";
        return jdbcRepository.execSql(selectSql);
    }

}
