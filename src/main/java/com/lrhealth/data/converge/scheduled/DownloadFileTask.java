package com.lrhealth.data.converge.scheduled;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.lrhealth.data.converge.common.util.file.FileUtils;
import com.lrhealth.data.converge.scheduled.dto.PreFileStatusDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
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

    public static CopyOnWriteArraySet<Integer> taskSet = new CopyOnWriteArraySet<>();

    public static AtomicInteger FILE_SIZE = new AtomicInteger();


    @Resource
    private Executor threadPoolTaskExecutor;

    private static final String url = "127.0.0.1:18081/file";

    @Scheduled(cron = "0/30 * * * * *")
    @Transactional
    public void refreshFENodesStatus() {
        //循环前置机
        //更新状态
        //添加任务
    }

    @PostConstruct
    public void loadTaskData() {
        taskSet.add(1);
        CompletableFuture.runAsync(this::downloadFile, threadPoolTaskExecutor);
    }

    public void downloadFile() {
        while (true) {
            if (taskSet.size() > 0) {
                //异步获取文件
                for (Integer taskId : taskSet) {
                    List<CompletableFuture<Void>> futureList = new ArrayList<>();
                    System.out.println("通知拆分：" + LocalDateTime.now());
                    //通知前置机文件拆分-压缩-加密
                    String s = HttpUtil.get(url + "/prepareFiles/" + taskId, 10000);

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
                        System.out.println("轮询状态：" + LocalDateTime.now());
                    }

                    System.out.println("开始下载：" + LocalDateTime.now());
                    //异步下载文件
                    for (Map.Entry<String, Integer> entry : preFileStatusDto.getPartFileMap().entrySet()) {
                        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                            int i = 0;
                            long l = HttpUtil.downloadFile(url + "/downloadFiles/" + entry.getKey(),
                                    "C:\\work\\" + entry.getKey());
                            while (l != entry.getValue() && i < 3) {
                                l = HttpUtil.downloadFile(url + "/downloadFiles/" + entry.getKey(),
                                        "C:\\work\\" + entry.getKey());
                                i++;
                            }
                            return null;
                        }, threadPoolTaskExecutor);
                        futureList.add(future);
                    }
                    //同步结果-校验-重试
                    List<Void> fileList = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                            .thenApply(e -> futureList.stream().map(CompletableFuture::join)
                                    .collect(Collectors.toList())).join();
                    System.out.println("下载完成：" + LocalDateTime.now());
                    // 文件解密-解压缩-合并
                    FileUtils fileUtils = new FileUtils();
                    FILE_SIZE.set(0);
                    try {
                        fileUtils.mergePartFiles("C:\\work\\", ".zip",
                                1024*1024*200, "C:\\work\\" + "new.csv", Base64Decoder.decode(s));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    while (true){
                        if(FILE_SIZE.get() == preFileStatusDto.getPartFileMap().size()){
                            System.out.println("合并完成：" + LocalDateTime.now());
                            break;
                        }
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                taskSet.clear();
            }
        }
    }
}
