package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.lrhealth.data.common.enums.conv.ConvergeTypeEnum;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.DataXExecDTO;
import com.lrhealth.data.converge.model.FileConvergeInfoDTO;
import com.lrhealth.data.converge.model.TaskDto;
import com.lrhealth.data.converge.service.ConvConfigService;
import com.lrhealth.data.converge.service.DataXService;
import com.lrhealth.data.converge.service.FileService;
import com.lrhealth.data.converge.service.XdsInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author jinmengyu
 * @date 2023-08-25
 */
@Service
public class DataXServiceImpl implements DataXService {
    private static ConcurrentMap<Long, DataXExecDTO> dataXConfigMap = new ConcurrentHashMap<>();
    @Resource
    ConvConfigService configService;
    @Resource
    XdsInfoService xdsInfoService;
    @Resource
    FileService fileService;
    @Override
    public DataXExecDTO createTask(TaskDto taskDto) {
        // dataX所需要的配置
        DataXExecDTO base = configService.getConfig(taskDto.getProjectId(), null, taskDto.getTaskModel());
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
            DataXExecDTO dataXExecDTO = dataXConfigMap.get(taskDto.getXdsId());
            if (ObjectUtil.isNull(dataXExecDTO)){
                throw new CommonException("xdsId配置不存在");
            }
            BeanUtil.copyProperties(dataXExecDTO, fileConfig);
            fileConfig.setOriFileName(taskDto.getOriFileName());
            fileConfig.setOriFileType("csv");
            fileService.fileConverge(fileConfig, taskDto.getXdsId());
        }
        return xds;
    }
}
