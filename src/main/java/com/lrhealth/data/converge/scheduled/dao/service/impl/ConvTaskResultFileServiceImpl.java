package com.lrhealth.data.converge.scheduled.dao.service.impl;

import cn.hutool.core.io.FileTypeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultFile;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.dao.mapper.DiConvTaskResultFileMapper;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskResultFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhangguowen-generator
 * @since 2023-11-06
 */
@Service
public class ConvTaskResultFileServiceImpl extends ServiceImpl<DiConvTaskResultFileMapper, ConvTaskResultFile> implements ConvTaskResultFileService {

    @Value("${file-collect.structure-type}")
    private String structureTypeStr;

    @Override
    public void createTaskResultFile(ConvTunnel tunnel, Integer taskId, File file, String odsModelName) {
        ConvTaskResultFile build = ConvTaskResultFile.builder()
                .taskId(taskId)
                .tableName(odsModelName)
                .createTime(LocalDateTime.now())
                .delFlag(0)
                .feStoredFilename(file.getName())
                .feStoredPath(file.getAbsolutePath())
                .fileType(structureTypeStr.contains(FileTypeUtil.getType(file)) ? 1 : 2)
                .dataSize(file.length())
                .status(1)
                .build();
        this.save(build);
        this.getOne(new LambdaQueryWrapper<ConvTaskResultFile>().eq(ConvTaskResultFile::getFeStoredFilename, file.getName()));
    }
}
