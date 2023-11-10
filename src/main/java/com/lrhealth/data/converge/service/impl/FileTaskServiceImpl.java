package com.lrhealth.data.converge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.enums.ExecStatusEnum;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultFile;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskResultFileService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.scheduled.thread.AsyncFactory;
import com.lrhealth.data.converge.service.FileTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jinmengyu
 * @date 2023-11-03
 */
@Service
public class FileTaskServiceImpl implements FileTaskService {
    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private ConvTaskResultFileService taskResultFileService;
    @Override
    public void run(Long tunnelId, Integer taskId, ExecStatusEnum execStatus, Integer oldTaskId) {
        ConvTunnel tunnel = tunnelService.getById(tunnelId);
        List<String> fileRangeList = Arrays.asList(tunnel.getCollectRange().split(","));
        if (execStatus.getValue() == 1){
            // 重新调度
            reFreshTaskResultFile(taskId, oldTaskId);
            return;
        }
        fileRangeList.forEach(odsModelName -> {
            // 已经采集完成的文件列表
            List<ConvTaskResultFile> resultFileList = taskResultFileService.list(new LambdaQueryWrapper<ConvTaskResultFile>()
                    .eq(ConvTaskResultFile::getTableName, odsModelName));
            Set<String> processFile = resultFileList.stream().distinct().map(ConvTaskResultFile::getFeStoredFilename).collect(Collectors.toSet());
            File fileList = new File(tunnel.getFileModeCollectDir());
            if (!fileList.exists()){
                throw new CommonException("管道文件地址不存在！");
            }
            if (!fileList.isDirectory() || fileList.isFile()){
                throw new CommonException("管道文件地址不是目录！");
            }
            List<File> newFileList = Arrays.asList(Objects.requireNonNull(fileList.listFiles((file, s) ->
                s.startsWith(odsModelName) && !processFile.contains(s)
            )));
            Set<String> newFileNameSet = newFileList.stream().map(File::getName).collect(Collectors.toSet());
            AsyncFactory.convTaskLog(taskId, "[" + odsModelName + "]新增了[" + newFileList.size() + "]个文件， 文件列表：" + newFileNameSet);
            newFileList.forEach(file ->
                // 生成taskResultFile
                taskResultFileService.createTaskResultFile(tunnel, taskId, file, odsModelName)
            );

        });

    }

    /**
     * 重新调度
     * 将之前的task生成一次新的任务实例
     * @param taskId
     */
    private void reFreshTaskResultFile(Integer taskId, Integer oldTaskId){
        List<ConvTaskResultFile> resultFileList = taskResultFileService.list(
                new LambdaQueryWrapper<ConvTaskResultFile>().eq(ConvTaskResultFile::getTaskId, oldTaskId));
        Set<String> fileNameSet = new HashSet<>();
        resultFileList.forEach(resultFile -> {
            ConvTaskResultFile frontendTaskResultFile = ConvTaskResultFile.builder()
                    .taskId(taskId).fileType(resultFile.getFileType())
                    .status(1).feStoredPath(resultFile.getFeStoredPath())
                    .feStoredFilename(resultFile.getFeStoredFilename())
                    .delFlag(0).createTime(LocalDateTime.now()).tableName(resultFile.getTableName()).build();
            taskResultFileService.save(frontendTaskResultFile);
            fileNameSet.add(frontendTaskResultFile.getFeStoredFilename());
        });
        AsyncFactory.convTaskLog(taskId, "重新调度了[" + oldTaskId + "]任务，采集的文件列表：" + fileNameSet);
    }
}
