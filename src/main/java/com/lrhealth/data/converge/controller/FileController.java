package com.lrhealth.data.converge.controller;

import com.lrhealth.data.converge.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * 文件处理
 * @author jinmengyu
 * @date 2023-07-27
 */
@Slf4j
@RestController()
@RequestMapping("/file")
public class FileController {
    @Resource
    private FileService fileService;

    @PostMapping("/upload")
    public void uploadFile(@RequestParam(value = "file") MultipartFile file,
                           @RequestParam(value = "projectId") String projectId){
        long start = System.currentTimeMillis();
        try {
            fileService.uploadFile(file, projectId);
        } catch (Exception e) {
            log.info("异常信息 {}",e.getMessage());
        }
        long end = System.currentTimeMillis();
        log.info("文件大小{}, 耗时 = {} ms, 上传速率{}/s",formatSize(file.getSize(),0,true),end - start,formatSize(file.getSize(),end-start,false));
    }


    /**
     * 统计上传文件速率/统计文件大小
     * @param size 文件大小 byte
     * @param costTime 耗时 ms
     * @param isSize 是否计算文件大小
     * @return 标准值结果
     */
    private static String formatSize(long size,long costTime,boolean isSize) {
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int baseSize = 1024;
        int index = 0;
        double formattedSize = size;
        while (formattedSize > baseSize && index < units.length - 1) {
            formattedSize /= baseSize;
            index++;
        }
        if (!isSize){
            double second = costTime / 1000.0;
            formattedSize = formattedSize / second;
            if (formattedSize >= baseSize) {
                return formatSize((long) formattedSize,0,true);
            }
        }
        return String.format("%.00f %s", formattedSize, units[index]);
    }

}
