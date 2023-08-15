package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.common.result.ResultBase;
import com.lrhealth.data.converge.model.FileInfo;
import com.lrhealth.data.converge.service.FepService;
import com.lrhealth.data.model.enums.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.text.CharSequenceUtil.isBlank;

/**
 * <p>
 * 前置机处理实现类
 * </p>
 *
 * @author lr
 * @since 2023/7/19 11:49
 */
@Service
@Slf4j
public class FepServiceImpl implements FepService {

    @Value("${fep.ip}")
    private String frontendIp;

    @Value("${fep.port}")
    private String frontendPort;

    @Value("${fep.fileScan}")
    private String fileScan;

    @Override
    public List<FileInfo> getFepFileList(String oriFilePath) {
        return scanFiles(oriFilePath);
    }

    @Override
    public List<FileInfo> fepFileList(String filePath) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("filePath", filePath);
        String responseData = HttpUtil.get(  "http://" + frontendIp + ":" + frontendPort + fileScan, jsonMap);
        ResultBase<List<FileInfo>> resultBase = JSON.toJavaObject(JSON.parseObject(responseData), ResultBase.class);
        log.info("扫描目录结果: {}", resultBase.getValue());
        return resultBase.getValue();
    }


    private List<FileInfo> scanFiles(String filePath) {
        if (isBlank(filePath)) {
            log.error("collectFilePath is null");
            throw new CommonException(ResultEnum.FAIL_PARAMETER_VALIDATION.getDesc());
        }
        File baseDir = FileUtil.file(filePath);
        // 检查目录信息是否为空
        if (!baseDir.isDirectory()) {
            log.error("filePath not exists: {} not a directory", baseDir);
            throw new CommonException("参数校验失败");
        }
        // 获得filePath目录下所有的文件和目录的绝对路径
        File[] files = baseDir.listFiles();
        if (null == files || files.length == 0) {
            log.info("collectFilePath : [{}] have no files", filePath);
            throw new CommonException("扫描目录暂无文件存在");
        }
        List<FileInfo> fileInfoList = CollUtil.newArrayList();
        for (File file : files) {
            if (file.isFile()) {
                FileInfo fileInfo = FileInfo.builder().fileName(file.getName()).fileSize(file.length()).lastModified(file.lastModified()).build();
                fileInfoList.add(fileInfo);
            }
        }
        return fileInfoList;
    }

}
