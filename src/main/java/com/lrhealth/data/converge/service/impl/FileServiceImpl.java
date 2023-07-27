package com.lrhealth.data.converge.service.impl;

import com.lrhealth.data.converge.dao.entity.ConvergeConfig;
import com.lrhealth.data.converge.dao.entity.ProjectConvergeRelation;
import com.lrhealth.data.converge.dao.service.ConvergeConfigService;
import com.lrhealth.data.converge.service.FileService;
import com.lrhealth.data.converge.service.ProjectConvergeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author jinmengyu
 * @date 2023-07-27
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Resource
    private ProjectConvergeService projectConvergeService;

    @Resource
    private ConvergeConfigService configService;

    @Override
    public void uploadFile(String localPath, String projectId) {
        ConvergeConfig convergeConfig = getConfig(projectId);

        File file = new File(localPath);
        String filename = file.getName();
        Path targetUrl = Paths.get( convergeConfig.getStoredFilePath() + "/" + filename);
        log.info("源文件目录: {} ---> 上传文件目录: {}", localPath, targetUrl);

        try (InputStream inputStream = Files.newInputStream(Paths.get(localPath))) {
            Files.copy(inputStream, targetUrl, StandardCopyOption.REPLACE_EXISTING);
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    /**
     * 获取配置信息
     *
     * @param projId 项目ID
     * @return 配置信息
     */
    private ConvergeConfig getConfig(String projId) {
        ProjectConvergeRelation relation = projectConvergeService.getByProjId(projId);
        return configService.getById(relation.getConvergeId());
    }
}
