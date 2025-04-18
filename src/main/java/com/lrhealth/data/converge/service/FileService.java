package com.lrhealth.data.converge.service;

import com.alibaba.fastjson.JSONObject;
import com.lrhealth.data.converge.dao.entity.Xds;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author jinmengyu
 * @date 2023-07-27
 */
public interface FileService {

    void uploadFile(MultipartFile file, String path);

    Long jsonDataSave(JSONObject jsonObject, Xds xds);
}
