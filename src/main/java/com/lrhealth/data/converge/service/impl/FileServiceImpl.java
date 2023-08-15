package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.lrhealth.data.converge.dao.entity.ConvergeConfig;
import com.lrhealth.data.converge.dao.entity.ProjectConvergeRelation;
import com.lrhealth.data.converge.dao.service.ConvergeConfigService;
import com.lrhealth.data.converge.dao.service.FrontendService;
import com.lrhealth.data.converge.service.FileService;
import com.lrhealth.data.converge.service.ProjectConvergeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static cn.hutool.core.text.StrPool.SLASH;

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
    @Resource
    private FrontendService frontendService;

    @Override
    public void uploadFile(MultipartFile file, String projectId) {
        String oriFilePath = getConfig(projectId);

        String filename = file.getOriginalFilename();
        Path targetUrl = Paths.get( oriFilePath + SLASH + filename);
        log.info("源文件名称: {} ---> 上传文件目录: {}", file.getName(), targetUrl);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetUrl, StandardCopyOption.REPLACE_EXISTING);
        }catch (Exception e){
            log.error(ExceptionUtil.stacktraceToString(e));
        }
    }

    /**
     * 获取配置信息
     *
     * @param projId 项目ID
     * @return 配置信息
     */
    private String getConfig(String projId) {
        ProjectConvergeRelation relation = projectConvergeService.getByProjId(projId);
        ConvergeConfig convergeConfig =  configService.getById(relation.getConvergeId());
        return frontendService.getByFrontenfCode(convergeConfig.getFrontendCode()).getFilePath();
    }
}
