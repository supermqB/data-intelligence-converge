package com.lrhealth.data.converge.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author jinmengyu
 * @date 2023-10-23
 */
public interface DictConvergeService {

    void dictConverge(MultipartFile file, String orgCode, String sysCode);

}
