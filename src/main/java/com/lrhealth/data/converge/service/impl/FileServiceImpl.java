package com.lrhealth.data.converge.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.lrhealth.data.converge.common.exception.CommonException;
import com.lrhealth.data.converge.dao.adpter.BeeBaseRepository;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author jinmengyu
 * @date 2023-07-27
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {
    @Resource
    private BeeBaseRepository beeBaseRepository;


    @Override
    public void uploadFile(MultipartFile file, String path) {
        Path targetUrl = Paths.get(path);
        log.info("源文件名称: {} ---> 上传文件目录: {}", file.getName(), targetUrl);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetUrl, StandardCopyOption.REPLACE_EXISTING);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }


    @Override
    public Long jsonDataSave(JSONObject jsonObject, Xds xds){
        Set<String> odsTableNames = jsonObject.keySet();
        long countNumber = 0;
        for (String odsTableName : odsTableNames) {
            try {
                List<Map<String, Object>> odsDataList = (List<Map<String, Object>>) jsonObject.get(odsTableName);
                odsDataList.forEach(map -> map.put("xds_id", xds.getId()));
                beeBaseRepository.insertBatch(xds.getSysCode(), xds.getOdsTableName(), odsDataList);
                countNumber += odsDataList.size();
            }catch (Exception e) {
                log.error("file data to db sql exception,{}", ExceptionUtils.getStackTrace(e));
                throw new CommonException("数据插入异常, xdsId: {}", String.valueOf(xds.getId()));
            }
        }
        return countNumber;
    }
}
