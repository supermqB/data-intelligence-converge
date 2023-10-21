package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.lrhealth.data.common.enums.conv.ConvergeTypeEnum;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.FileConvergeInfoDTO;
import com.lrhealth.data.converge.model.FileExecInfoDTO;
import com.lrhealth.data.converge.model.TaskDto;
import com.lrhealth.data.converge.service.*;
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
    ConvConfigService configService;
    @Resource
    XdsInfoService xdsInfoService;
    @Resource
    FileService fileService;
    @Resource
    ShellService shellService;
    @Override
    public FileExecInfoDTO createTask(TaskDto taskDto) {
        // dataX所需要的配置
        FileExecInfoDTO base = configService.getConfig(taskDto.getProjectId(), null, taskDto.getTaskModel());
        Xds xds = xdsInfoService.createXdsInfo(taskDto, base);
        base.setXdsId(xds.getId());
        base.setOdsModelName(xds.getOdsModelName());
        if (!dataXConfigMap.containsKey(xds.getId())){
            dataXConfigMap.put(xds.getId(), base);
        }
        return base;
    }

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


    @Override
    public void execDataX(String json, String jsonSavePath) {
        String[] jsonList = json.split("\\s+");
        for (String execJson : jsonList){
            try {
                shellService.dataXExec(execJson, jsonSavePath);
            }catch (Exception e){
                log.error("dataX execute error, table:{}", execJson);
            }
        }
    }

}
