package com.lrhealth.data.converge.scheduled;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.util.MinioClientUtils;
import com.lrhealth.data.converge.common.util.thread.AsyncFactory;
import com.lrhealth.data.converge.common.util.thread.AsyncManager;
import com.lrhealth.data.converge.dao.entity.ConvTaskLog;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.model.FileTask;
import com.lrhealth.data.converge.model.TaskFileConfig;
import com.lrhealth.data.converge.model.dto.PreFileStatusDto;
import com.lrhealth.data.converge.service.ConvergeService;
import com.lrhealth.data.converge.service.TaskFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author zhaohui
 * @version 1.0
 * 文件下载任务
 */
@Component
@EnableScheduling
public class DownloadFileTask {

    private static final Logger log = LoggerFactory.getLogger(DownloadFileTask.class);

    @Resource
    private ConvergeService convergeService;

    @Resource
    private TaskFileService taskFileService;
    @Resource
    private ConvTunnelService convTunnelService;

    @Resource
    private Executor threadPoolTaskExecutor;
    @Resource
    private MinioClientUtils minioClientUtils;

    public static final ConcurrentLinkedDeque<FileTask> taskDeque = new ConcurrentLinkedDeque<>();


//   @Scheduled(cron = "${lrhealth.converge.scheduledCron}")
    public void refreshFENodesStatus() {
        //循环前置机
        log.info("定时更新前置机任务状态！" + LocalDateTime.now());
        List<ConvTunnel> tunnelList = convTunnelService.list(new LambdaQueryWrapper<ConvTunnel>()
                .ne(ConvTunnel::getStatus, 0)
                .ne(ConvTunnel::getStatus, 4)
                .ne(ConvTunnel::getFrontendId, -1L));
        List<Long> frontendIdList =
                tunnelList.stream().map(ConvTunnel::getFrontendId).distinct().collect(Collectors.toList());
        convergeService.updateDownLoadFileTask(taskDeque);
        for (Long id : frontendIdList) {
            convergeService.updateFeNodeStatus(id, taskDeque);
        }
    }

    @PostConstruct
    public void loadTaskData() {
        threadPoolTaskExecutor.execute(this::downloadFile);
    }

    public void downloadFile() {
        while (true) {
            FileTask fileTask = getFileTask();
            if (fileTask == null) {
                continue;
            }

            TaskFileConfig taskFileConfig = convergeService.getTaskConfig(fileTask);

            //创建工作目录
            if (!createWorkPath(taskFileConfig.getDestPath())) {
                //失败重置任务
                resetFileTask(fileTask, taskFileConfig);
                continue;
            }
            String objectStoragePath = null;

            convTaskLog(fileTask.getTaskId(), "开始下载：" + fileTask);
            long startTime = System.currentTimeMillis();

            if (ObjectUtil.isNotNull(taskFileConfig.getTaskResultView())){
                convTaskLog(fileTask.getTaskId(), "通知前置机文件分片：" + fileTask);
                if (!taskFileService.splitFile(taskFileConfig)) {
                    log.error("通知拆分文件异常：" + fileTask);
                    resetFileTask(fileTask, taskFileConfig);
                    continue;
                }

                convTaskLog(fileTask.getTaskId(), "正在压缩、加密文件：" + fileTask);
                PreFileStatusDto preFileStatusDto = taskFileService.getFileStatus(taskFileConfig);
                if (preFileStatusDto == null){
                    log.error("文件拆分异常：" + fileTask);
                    resetFileTask(fileTask, taskFileConfig);
                    continue;
                }

                if (!taskFileService.downloadFile(preFileStatusDto,taskFileConfig)){
                    log.error("文件下载失败：" + fileTask);
                    resetFileTask(fileTask, taskFileConfig);
                    continue;
                }
                convTaskLog(fileTask.getTaskId(), "开始合并文件：" + fileTask);
                if (!taskFileService.mergeFile(taskFileConfig)){
                    log.error("文件合并失败：" + fileTask);
                    resetFileTask(fileTask, taskFileConfig);
                    continue;
                }
            }
            if (ObjectUtil.isNotNull(taskFileConfig.getTaskResultFile())){
                objectStoragePath = taskFileConfig.getTaskResultFile().getFeStoredPath();
            }

            if (ObjectUtil.isNotNull(taskFileConfig.getTaskResultFile()) && !minioClientUtils.download(objectStoragePath, fileTask.getFileName(),taskFileConfig.getDestPath())){
                log.error("文件下载失败：" + fileTask);
                resetFileTask(fileTask, taskFileConfig);
                continue;
            }


            long endTime = System.currentTimeMillis();

            //            log.info("开始删除文件：" + fileTask);
//            if (!taskFileService.deleteFile(taskFileConfig)){
//                log.error("文件删除失败：" + fileTask);
//                resetFileTask(fileTask, taskFileConfig);
//                continue;
//            }
            //更新文件状态
            if (!convergeService.updateFileStatus(taskFileConfig,endTime - startTime)){
                log.error("文件状态更新失败：" + fileTask);
                resetFileTask(fileTask, taskFileConfig);
                continue;
            }
            convTaskLog(fileTask.getTaskId(), "文件状态更新成功！" + fileTask);
        }
    }


    // 备份之前库到文件的方式
//    public void downloadFile() {
//        while (true) {
//            FileTask fileTask = getFileTask();
//            if (fileTask == null) {
//                continue;
//            }
//
//            TaskFileConfig taskFileConfig = convergeService.getTaskConfig(fileTask);
//
//            //创建工作目录
//            if (!createWorkPath(taskFileConfig.getDestPath())) {
//                //失败重置任务
//                resetFileTask(fileTask, taskFileConfig);
//                continue;
//            }
//
//            convTaskLog(fileTask.getTaskId(), "通知前置机文件分片：" + fileTask);
//            if (!taskFileService.splitFile(taskFileConfig)) {
//                log.error("通知拆分文件异常：" + fileTask);
//                resetFileTask(fileTask, taskFileConfig);
//                continue;
//            }
//
//            convTaskLog(fileTask.getTaskId(), "正在压缩、加密文件：" + fileTask);
//            PreFileStatusDto preFileStatusDto = taskFileService.getFileStatus(taskFileConfig);
//            if (preFileStatusDto == null){
//                log.error("文件拆分异常：" + fileTask);
//                resetFileTask(fileTask, taskFileConfig);
//                continue;
//            }
//
//            convTaskLog(fileTask.getTaskId(), "开始下载：" + fileTask);
//            long startTime = System.currentTimeMillis();
//            if (!taskFileService.downloadFile(preFileStatusDto,taskFileConfig)){
//                log.error("文件下载失败：" + fileTask);
//                resetFileTask(fileTask, taskFileConfig);
//                continue;
//            }
//
//            convTaskLog(fileTask.getTaskId(), "开始合并文件：" + fileTask);
//            if (!taskFileService.mergeFile(taskFileConfig)){
//                log.error("文件合并失败：" + fileTask);
//                resetFileTask(fileTask, taskFileConfig);
//                continue;
//            }
//            long endTime = System.currentTimeMillis();
//
////            log.info("开始删除文件：" + fileTask);
////            if (!taskFileService.deleteFile(taskFileConfig)){
////                log.error("文件删除失败：" + fileTask);
////                resetFileTask(fileTask, taskFileConfig);
////                continue;
////            }
//
//            //更新文件状态
//            if (!convergeService.updateFileStatus(taskFileConfig,endTime - startTime)){
//                log.error("文件状态更新失败：" + fileTask);
//                resetFileTask(fileTask, taskFileConfig);
//                continue;
//            }
//            convTaskLog(fileTask.getTaskId(), "文件状态更新成功！" + fileTask);
//        }
//    }

    private FileTask getFileTask() {
        FileTask fileTask = taskDeque.pollFirst();
        if (fileTask == null) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                log.error("等待任务队列异常！" + e.getMessage());
            }
        }
        return fileTask;
    }


    private boolean createWorkPath(String destPath) {
        File file = new File(destPath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                log.error("创建工作文件夹失败: " + destPath);
                return false;
            }
        }
        return true;
    }

    private void resetFileTask(FileTask fileTask, TaskFileConfig taskFileConfig) {
        taskDeque.add(fileTask);
        convergeService.resetStatus(taskFileConfig.getConvTask(), taskFileConfig);
        convTaskLog(fileTask.getTaskId(), "文件传输异常，正在重试！ | " + fileTask);
    }

    public static void convTaskLog(Integer taskId, String message){
        AsyncManager.me().execute(AsyncFactory.recordTaskLog(ConvTaskLog.builder()
                .taskId(taskId)
                .logDetail(message)
                .build()));
    }
}

