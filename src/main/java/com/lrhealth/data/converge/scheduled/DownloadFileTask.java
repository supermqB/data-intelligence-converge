package com.lrhealth.data.converge.scheduled;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
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
import com.lrhealth.data.converge.scheduled.utils.RsaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.concurrent.atomic.AtomicReference;
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

    @Value("${lrhealth.converge.privateKeyStr}")
    private String privateKeyStr;

    @Scheduled(cron = "0 0/30 * * * *")
    public void refreshFENodesStatus() {
        //循环前置机
        RSA instance = RsaUtils.getInstance(privateKeyStr);
        String token = "lrhealth:" + System.currentTimeMillis();

        List<ConvTunnel> tunnelList = convTunnelService.list(new LambdaQueryWrapper<ConvTunnel>()
                .ne(ConvTunnel::getStatus, 3));
        List<Long> frontendIdList =
                tunnelList.stream().map(ConvTunnel::getFrontendId).distinct().collect(Collectors.toList());

        for (Long id : frontendIdList) {
            CompletableFuture.runAsync(() ->{
                ConvFeNode node = convFeNodeService.getById(id);
                String url = node.getIp() + ":" +node.getPort() + "/task/frontend/status";
                String result = HttpRequest.get(url)
                        .header("Authorization",instance.encryptBase64(token, KeyType.PrivateKey))
                        .execute().body();
                System.out.println(result);

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
                    RSA instance = RsaUtils.getInstance(privateKeyStr);
                    AtomicReference<String> token = new AtomicReference<>("lrhealth:" + System.currentTimeMillis());

                    log.info("通知拆分：" + LocalDateTime.now());
                    //通知前置机文件拆分-压缩-加密
                    String result;
                    try {
                         result = HttpRequest.post(url + "/prepareFiles/" + taskId)
                                .header("Authorization",instance.encryptBase64(token.get(),KeyType.PrivateKey))
                                .body(JSONObject.toJSONString(new HashMap<String,Object>(){{
                                    put("zipFlag",tunnel.getZipFlag());
                                    put("encryptionFlag",tunnel.getEncryptionFlag());
                                    put("dataShardSize",tunnel.getDataShardSize());
                                }})).timeout(3000).execute().body();
                    }catch (Exception e){
                        log.error("任务：" + taskId + "通知拆分异常！");
                        continue;
                    }


                    PreFileStatusDto preFileStatusDto = null;
                    //查询拆分结果
                    while (true) {
                        try {
                            log.info("轮询状态：" + LocalDateTime.now());
                            token.set("lrhealth:" + System.currentTimeMillis());
                            String statusResponse = HttpRequest.get(url + "/prepareFiles/status/" + taskId)
                                    .header("Authorization",instance.encryptBase64(token.get(), KeyType.PrivateKey))
                                    .timeout(3000).execute().body();
                            preFileStatusDto = JSONObject.parseObject(statusResponse, PreFileStatusDto.class);
                            if ("1".equals(preFileStatusDto.getStatus())) {
                                break;
                            }
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            log.error("轮询任务:" + taskId + "异常！");
                            break;
                        }
                    }
                    if (preFileStatusDto == null){
                        continue;
                    }

                    log.info("开始下载：" + LocalDateTime.now());

                    //异步下载文件
                    List<CompletableFuture<Void>> futureList = new ArrayList<>();
                    for (Map.Entry<String, Integer> entry : preFileStatusDto.getPartFileMap().entrySet()) {
                        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                            int i = 0;
//                            long l = HttpUtil.downloadFile(url + "/downloadFiles/" + entry.getKey(),
//                                    destPath + entry.getKey());
                            token.set("lrhealth:" + System.currentTimeMillis());
                            HttpResponse execute = HttpRequest.get(url + "/downloadFiles/" + entry.getKey())
                                    .header("Authorization",instance.encryptBase64(token.get(), KeyType.PrivateKey))
                                    .execute();
                            long l = execute.writeBody(destPath + entry.getKey());
                            while (l != entry.getValue() && i < 3) {
                                execute = HttpRequest.get(url + "/downloadFiles/" + entry.getKey())
                                        .header("Authorization",instance.encryptBase64(token.get(), KeyType.PrivateKey))
                                        .execute();
                                l = execute.writeBody(destPath + entry.getKey());
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
                        log.error("任务："+ taskId + "合并失败！");
                        continue;
                    }

                    File file = FileUtil.file(taskResultView.getStoredPath());
                    while (true){
                        if(FILE_SIZE.get() == preFileStatusDto.getPartFileMap().size()
                        && file.length() == taskResultView.getDataSize()){
                            log.info("合并完成：" + LocalDateTime.now());
                            token.set("lrhealth:" + System.currentTimeMillis());
                            //通知删除
                            HttpRequest.get(url + "/deleteFiles/" + taskId)
                                    .header("Authorization",instance.encryptBase64(token.get(), KeyType.PrivateKey))
                                    .timeout(3000).execute().body();
                            break;
                        }
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            log.error("合并任务：" + taskId + "异常！");
                        }
                    }
                }
                taskDeque.clear();
            }

        }
    }
}
