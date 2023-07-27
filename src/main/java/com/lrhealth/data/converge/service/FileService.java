package com.lrhealth.data.converge.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author jinmengyu
 * @date 2023-07-27
 */
public interface FileService {

    void uploadFile(MultipartFile file, String projectId);
}
