package com.lrhealth.data.converge.scheduled;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.DataConvergeApplication;
import com.lrhealth.data.converge.common.util.file.FileUtils;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvFeNode;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTask;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultView;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.dao.service.*;
import com.lrhealth.data.converge.scheduled.model.FileTask;
import com.lrhealth.data.converge.scheduled.model.dto.FrontendStatusDto;
import com.lrhealth.data.converge.scheduled.model.dto.PreFileStatusDto;
import com.lrhealth.data.converge.scheduled.service.ConvergeService;
import com.lrhealth.data.converge.scheduled.utils.RsaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.record.FeatHdrRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
    private ConvFeNodeService convFeNodeService;

    @Resource
    private ConvTunnelService convTunnelService;

    @Resource
    private ConvTaskService convTaskService;

    @Resource
    private ConvTaskResultViewService convTaskResultViewService;

    @Resource
    private Executor threadPoolTaskExecutor;

    private static final ConcurrentLinkedDeque<FileTask> taskDeque = new ConcurrentLinkedDeque<>();
    public static AtomicInteger FILE_SIZE = new AtomicInteger();

    @Value("${lrhealth.converge.privateKeyStr}")
    private String privateKeyStr;

    @Value("${lrhealth.converge.path}")
    private String path;

    @Scheduled(cron = "0 * * * * *")
    public void refreshFENodesStatus() {
        //循环前置机
        log.info("定时更新前置机任务状态！" + LocalDateTime.now());
        List<ConvTunnel> tunnelList = convTunnelService.list(new LambdaQueryWrapper<ConvTunnel>()
                .ne(ConvTunnel::getStatus, 0)
                .ne(ConvTunnel::getStatus, 4));
        List<Long> frontendIdList =
                tunnelList.stream().map(ConvTunnel::getFrontendId).distinct().collect(Collectors.toList());
         convergeService.updateDownLoadFileTask(taskDeque);
        for (Long id : frontendIdList) {
            convergeService.updateFeNodeStatus(id,taskDeque);
        }
    }

    @PostConstruct
    public void loadTaskData() {
        threadPoolTaskExecutor.execute(this::downloadFile);
    }

    public void downloadFile() {
        while (true) {
            FileTask fileTask = taskDeque.peekFirst();
            if (fileTask == null) {
                try {
                    Thread.sleep(3000);
                }catch (Exception ignored){
                }
                continue;
            }

            int taskId = fileTask.getTaskId();
            String fileName = fileTask.getFileName();

            ConvTask convTask = convTaskService.getById(taskId);
            ConvTunnel tunnel = convTunnelService.getById(convTask.getTunnelId());
            ConvFeNode feNode = convFeNodeService.getById(tunnel.getFrontendId());
            ConvTaskResultView taskResultView = convTaskResultViewService.getOne(new LambdaQueryWrapper<ConvTaskResultView>()
                    .eq(ConvTaskResultView::getTaskId, taskId)
                    .eq(ConvTaskResultView::getFeStoredFilename,fileName));
            String url = feNode.getIp() + ":" + feNode.getPort() + "/file";

            FileTask frontNodeTask = new FileTask(convTask.getFedTaskId(),fileName);
            String destPath = path + File.separator + fileTask.getTaskId()
                    + File.separator + fileTask.getFileName() + File.separator;
            File file =
                    new File(destPath);
            if (!file.exists()){
                if (!file.mkdirs()){
                    log.error("创建文件夹失败！");
                    taskDeque.add(fileTask);
                    taskDeque.pollFirst();
                    continue;
                }
            }

            log.info("通知拆分文件：" + LocalDateTime.now() + " " + fileName);
            //通知前置机文件拆分-压缩-加密
            String result;
            try {
                result = convergeService.prepareFiles(url,frontNodeTask);
            } catch (Exception e) {
                log.error("任务：" + taskId + "通知拆分异常！\n" + e.getMessage());
                taskDeque.add(fileTask);
                taskDeque.pollFirst();
                convergeService.resetStatus(convTask,taskResultView);
                continue;
            }
            if (!"true".equals(result)){
                log.error("任务：" + taskId + "通知拆分异常！");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
                taskDeque.add(fileTask);
                taskDeque.pollFirst();
                convergeService.resetStatus(convTask,taskResultView);
                continue;
            }


            PreFileStatusDto preFileStatusDto = null;
            int i = 0;
            //查询拆分结果
            while (i < (20 * 60 * 2)) {
                try {
                    log.info("轮询状态：" + LocalDateTime.now() + " " + fileName);
                    preFileStatusDto = convergeService.getPreFilesStatus(url,frontNodeTask);
                    if (preFileStatusDto == null || "1".equals(preFileStatusDto.getStatus())) {
                        break;
                    }
                    i++;
                    Thread.sleep(5000);
                } catch (Exception e) {
                    log.error("轮询任务:" + taskId + "异常！\n" + e.getMessage());
                    taskDeque.add(fileTask);
                    taskDeque.pollFirst();
                    convergeService.resetStatus(convTask,taskResultView);
                    break;
                }
            }
            if (preFileStatusDto == null || i >= (12 * 60 * 2)) {
                log.error("轮询任务:" + taskId + "异常！");
                taskDeque.add(fileTask);
                taskDeque.pollFirst();
                convergeService.resetStatus(convTask,taskResultView);
                continue;
            }

            //异步下载文件
            log.info("开始下载：" + LocalDateTime.now() + " " + fileName);
            long startTime = System.currentTimeMillis();
            long endTime = System.currentTimeMillis();
            convergeService.downLoadFile(url, file, frontNodeTask,preFileStatusDto);
            log.info("下载完成：" + LocalDateTime.now() + " " + fileName);

            // 文件解密-解压缩-合并
            FileUtils fileUtils = new FileUtils();
            FILE_SIZE.set(0);
            try {
                fileUtils.mergePartFiles(destPath, ".part",
                        tunnel.getDataShardSize().intValue(), destPath + File.separator
                                + fileName,
                        Base64Decoder.decode(feNode.getAesKey()));
            } catch (Exception e) {
                log.error("任务：" + taskId + "合并失败！\n" + e.getMessage());
                taskDeque.add(fileTask);
                taskDeque.pollFirst();
                convergeService.resetStatus(convTask,taskResultView);
                continue;
            }

            File destFile = FileUtil.file(destPath + File.separator
                    + fileName);
            i = 0;
            while (i < (20 * 60 * 2)) {
                log.info("正在合并文件..." + LocalDateTime.now());
                if (FILE_SIZE.get() == preFileStatusDto.getPartFileMap().size()
                        && destFile.length() == taskResultView.getDataSize()) {
                    log.info("合并完成：" + LocalDateTime.now()+ " " + fileName);
                     endTime = System.currentTimeMillis();
                    convergeService.deleteFiles(url,frontNodeTask);
                    break;
                }
                try {
                    Thread.sleep(3000);
                    i++;
                } catch (Exception e) {
                    log.error("删除文件：" + taskId + "异常！\n" + e.getMessage());
                    convergeService.resetStatus(convTask,taskResultView);
                    break;
                }
            }
            if (i >= 20 * 60 * 2){
                System.out.println("合并文件超时！" + path + fileName);
                taskDeque.add(fileTask);
                taskDeque.pollFirst();
                convergeService.resetStatus(convTask,taskResultView);
                continue;
            }
            //跟新文件状态
            taskDeque.pollFirst();
            taskResultView.setTransferTime(endTime - startTime);
            convergeService.updateFileStatus(taskResultView);
            log.info("文件状态更新成功！" + fileName);
        }
    }

}

