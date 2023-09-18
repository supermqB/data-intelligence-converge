package com.lrhealth.data.converge.scheduled;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.util.file.FileUtils;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvFeNode;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTask;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultView;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.dao.service.ConvFeNodeService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskResultViewService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.scheduled.dto.PreFileStatusDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
import java.util.stream.Collectors;

/**
 * @author zhaohui
 * @version 1.0
 * 文件下载任务
 */
@Slf4j
@Component
@EnableScheduling
public class DownloadFileTask {

    @Resource
    private ConvFeNodeService convFeNodeService;

    @Resource
    private ConvTunnelService convTunnelService;

    @Resource
    private ConvTaskService convTaskService;

    @Resource
    private ConvTaskResultViewService convTaskResultViewService;

    private final ConcurrentLinkedDeque<Integer> taskDeque = new ConcurrentLinkedDeque<>();
    public static AtomicInteger FILE_SIZE = new AtomicInteger();

    private String destPath;

    @Resource
    private Executor threadPoolTaskExecutor;

    @Scheduled(cron = "0/30 * * * * *")
    public void refreshFENodesStatus() {
        //循环前置机
        List<ConvTunnel> tunnelList = convTunnelService.list(new LambdaQueryWrapper<ConvTunnel>()
                .ne(ConvTunnel::getStatus, "3"));
        List<Long> frontendIdList =
                tunnelList.stream().map(ConvTunnel::getFrontendId).distinct().collect(Collectors.toList());

        for (Long id : frontendIdList) {
            CompletableFuture.runAsync(() ->{
                ConvFeNode node = convFeNodeService.getById(id);
                String url = node.getIp() + ":" +node.getPort() + "/task/frontend/status";
                String result = HttpUtil.get(url);

                //更新状态
                //添加任务

            }, threadPoolTaskExecutor);
        }
    }

    @PostConstruct
    public void loadTaskData() {
        taskDeque.add(1);
        CompletableFuture.runAsync(this::downloadFile, threadPoolTaskExecutor);
    }

    public void downloadFile() {
        while (true) {
            if (taskDeque.size() > 0) {
                //异步获取文件
                for (Integer taskId : taskDeque) {

                    ConvTask convTask = convTaskService.getById(taskId);
                    ConvTunnel tunnel = convTunnelService.getById(convTask.getTunnelId());
                    ConvFeNode feNode = convFeNodeService.getById(tunnel.getFrontendId());
                    ConvTaskResultView taskResultView = convTaskResultViewService.getOne(new LambdaQueryWrapper<ConvTaskResultView>()
                            .eq(ConvTaskResultView::getTaskId, taskId));
                    String fileName = "";
                    String url = feNode.getIp() + ":" + feNode.getPort();

                    log.info("通知拆分：" + LocalDateTime.now());
                    //通知前置机文件拆分-压缩-加密
                    String result = HttpUtil.post(url + "/prepareFiles/" + taskId,
                            new HashMap<String,Object>(){{
                                put("zipFlag",tunnel.getZipFlag());
                                put("encryptionFlag",tunnel.getEncryptionFlag());
                                put("dataShardSize",tunnel.getDataShardSize());
                            }}
                            ,10000);

                    PreFileStatusDto preFileStatusDto;
                    //查询拆分结果
                    while (true) {
                        String statusResponse = HttpUtil.get(url + "/prepareFiles/status/" + taskId, 10000);
                        preFileStatusDto = JSONObject.parseObject(statusResponse, PreFileStatusDto.class);
                        if ("1".equals(preFileStatusDto.getStatus())) {
                            break;
                        }
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        log.info("轮询状态：" + LocalDateTime.now());
                    }

                    log.info("开始下载：" + LocalDateTime.now());

                    //异步下载文件
                    List<CompletableFuture<Void>> futureList = new ArrayList<>();
                    for (Map.Entry<String, Integer> entry : preFileStatusDto.getPartFileMap().entrySet()) {
                        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                            int i = 0;
                            long l = HttpUtil.downloadFile(url + "/downloadFiles/" + entry.getKey(),
                                    destPath + entry.getKey());
                            while (l != entry.getValue() && i < 3) {
                                l = HttpUtil.downloadFile(url + "/downloadFiles/" + entry.getKey(),
                                        destPath + entry.getKey());
                                i++;
                            }
                            return null;
                        }, threadPoolTaskExecutor);
                        futureList.add(future);
                    }
                    //同步结果-校验-重试
                    CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                            .thenApply(e -> futureList.stream().map(CompletableFuture::join)
                                    .collect(Collectors.toList())).join();
                    log.info("下载完成：" + LocalDateTime.now());
                    // 文件解密-解压缩-合并
                    FileUtils fileUtils = new FileUtils();
                    FILE_SIZE.set(0);
                    try {
                        fileUtils.mergePartFiles(destPath, ".part",
                                tunnel.getDataShardSize().intValue(), destPath + File.separator
                                        + fileName,
                                Base64Decoder.decode(result));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    File file = FileUtil.file(taskResultView.getStoredPath());
                    while (true){
                        if(FILE_SIZE.get() == preFileStatusDto.getPartFileMap().size()
                        && file.length() == taskResultView.getDataSize()){
                            log.info("合并完成：" + LocalDateTime.now());
                            //通知删除
                            HttpUtil.get(url + "/deleteFiles/" + taskId, 10000);
                            break;
                        }
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                taskDeque.clear();
            }
        }
    }
}
