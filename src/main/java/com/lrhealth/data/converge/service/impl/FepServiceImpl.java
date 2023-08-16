package com.lrhealth.data.converge.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.lrhealth.data.common.result.ResultBase;
import com.lrhealth.data.converge.model.FileInfo;
import com.lrhealth.data.converge.service.FepService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public List<FileInfo> fepFileList(String filePath) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("filePath", filePath);
        String responseData = HttpUtil.get(  "http://" + frontendIp + ":" + frontendPort + fileScan, jsonMap);
        ResultBase<List<FileInfo>> resultBase = JSON.toJavaObject(JSON.parseObject(responseData), ResultBase.class);
        log.info("扫描目录结果: {}", resultBase.getValue());
        return resultBase.getValue();
    }

}
