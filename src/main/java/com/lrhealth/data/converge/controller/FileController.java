package com.lrhealth.data.converge.controller;

import com.lrhealth.data.converge.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
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
        fileService.uploadFile(file, projectId);
    }
}
