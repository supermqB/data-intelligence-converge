package com.lrhealth.data.converge.service;

import com.alibaba.fastjson.JSONObject;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.FileConvergeInfoDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author jinmengyu
 * @date 2023-07-27
 */
public interface FileService {

    void uploadFile(MultipartFile file, String projectId);

    /**
     * 前置机扫描 -> 文件搬运 -> 文件解析 -> 文件落库
     * 直连模式：前置机与汇聚在同一服务器
     * 前置机模式：前置机与汇聚在不同服务器
     * @param fileConfig
     * @param xdsId
     * @return
     */
    Xds fileConverge(FileConvergeInfoDTO fileConfig, Long xdsId);

    /**
     * flinkCDC采集的文件内部模板不同，不能跟正常格式一同处理
     * @param fileConfig
     * @param xdsId
     * @return
     */
    Xds flinkFileConverge(FileConvergeInfoDTO fileConfig, Long xdsId);

    Integer jsonDataSave(JSONObject jsonObject, Xds xds);
}
