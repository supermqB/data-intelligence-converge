package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.lrhealth.data.common.enums.conv.ConvergeTypeEnum;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.FileConvergeInfoDTO;
import com.lrhealth.data.converge.model.FileExecInfoDTO;
import com.lrhealth.data.converge.model.TaskDto;
import com.lrhealth.data.converge.service.DataXService;
import com.lrhealth.data.converge.service.FileService;
import com.lrhealth.data.converge.service.ShellService;
import com.lrhealth.data.converge.service.XdsInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author jinmengyu
 * @date 2023-08-25
 */
@Service
@Slf4j
public class DataXServiceImpl implements DataXService {
    private static ConcurrentMap<Long, FileExecInfoDTO> dataXConfigMap = new ConcurrentHashMap<>();
    @Resource
    XdsInfoService xdsInfoService;
    @Resource
    FileService fileService;
    @Resource
    ShellService shellService;

    @Override
    public Xds updateTask(TaskDto taskDto) {
        Xds xds = xdsInfoService.updateXdsCompleted(taskDto);
        if (ConvergeTypeEnum.isFile(taskDto.getTaskModel())){
            // 开始文件解析流程
            FileConvergeInfoDTO fileConfig = new FileConvergeInfoDTO();
            FileExecInfoDTO fileExecInfoDTO = dataXConfigMap.get(taskDto.getXdsId());
            if (ObjectUtil.isNull(fileExecInfoDTO)){
                throw new CommonException("xdsId配置不存在");
            }
            BeanUtil.copyProperties(fileExecInfoDTO, fileConfig);
            fileConfig.setOriFileName(taskDto.getOriFileName());
            fileConfig.setOriFileType("csv");
            fileService.fileConverge(fileConfig, taskDto.getXdsId());
        }
        return xds;
    }
}
