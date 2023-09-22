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

    @Scheduled(cron = "0/5 * * * * *")
    @Transactional
    public void refreshFENodesStatus() {
        //循环前置机
        System.out.println("定时更新前置机任务状态！");
        List<ConvTunnel> tunnelList = convTunnelService.list(new LambdaQueryWrapper<ConvTunnel>()
                .ne(ConvTunnel::getStatus, 0)
                .ne(ConvTunnel::getStatus, 4));
        List<Long> frontendIdList =
                tunnelList.stream().map(ConvTunnel::getFrontendId).distinct().collect(Collectors.toList());
        convergeService.updateDownLoadFileTask(taskDeque);
        for (Long id : frontendIdList) {
            CompletableFuture.runAsync(() -> convergeService.updateFeNodeStatus(id,taskDeque), threadPoolTaskExecutor);
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
            log.info("通知拆分：" + LocalDateTime.now());
            //通知前置机文件拆分-压缩-加密
            String result;
            try {
                result = convergeService.prepareFiles(url,frontNodeTask);
            } catch (Exception e) {
                log.error("任务：" + taskId + "通知拆分异常！");
                continue;
            }
            if (!"true".equals(result)){
                log.error("任务：" + taskId + "通知拆分异常！");
                continue;
            }


            PreFileStatusDto preFileStatusDto = null;
            //查询拆分结果
            while (true) {
                try {
                    log.info("轮询状态：" + LocalDateTime.now());
                    preFileStatusDto = convergeService.getPreFilesStatus(url,frontNodeTask);
                    if (preFileStatusDto == null || "1".equals(preFileStatusDto.getStatus())) {
                        break;
                    }
                    Thread.sleep(3000);
                } catch (Exception e) {
                    log.error("轮询任务:" + taskId + "异常！");
                    break;
                }
            }
            if (preFileStatusDto == null) {
                log.error("轮询任务:" + taskId + "异常！");
                continue;
            }

            //异步下载文件
            log.info("开始下载：" + LocalDateTime.now());
            convergeService.downLoadFile(url, frontNodeTask.getTaskId() ,preFileStatusDto);
            log.info("下载完成：" + LocalDateTime.now());

            // 文件解密-解压缩-合并
            FileUtils fileUtils = new FileUtils();
            FILE_SIZE.set(0);
            try {
                System.out.println(feNode.getAesKey());
                fileUtils.mergePartFiles(path, ".part",
                        tunnel.getDataShardSize().intValue(), path + File.separator
                                + fileName,
                        Base64Decoder.decode(feNode.getAesKey()));
            } catch (Exception e) {
                log.error("任务：" + taskId + "合并失败！");
                continue;
            }

            File file = FileUtil.file(taskResultView.getStoredPath());
            while (true) {
                if (FILE_SIZE.get() == preFileStatusDto.getPartFileMap().size()
                        && file.length() == taskResultView.getDataSize()) {
                    log.info("合并完成：" + LocalDateTime.now());
                    convergeService.deleteFiles(url,frontNodeTask);
                    break;
                }
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    log.error("删除文件：" + taskId + "异常！");
                }
            }
            //跟新文件状态
            taskDeque.pollFirst();
            convergeService.updateFileStatus(taskResultView);
        }
    }

}

