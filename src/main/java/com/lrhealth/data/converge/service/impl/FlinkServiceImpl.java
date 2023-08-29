package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.lrhealth.data.converge.dao.adpter.BeeBaseRepository;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.DataXExecDTO;
import com.lrhealth.data.converge.model.FileConvergeInfoDTO;
import com.lrhealth.data.converge.model.TaskDto;
import com.lrhealth.data.converge.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jinmengyu
 * @date 2023-08-08
 */
@Service
@Slf4j
public class FlinkServiceImpl implements FlinkService {
    @Resource
    private BeeBaseRepository beeBaseRepository;
    @Resource
    private XdsInfoService xdsInfoService;

    @Resource
    private FileService fileService;

    @Override
    public Xds database(Xds xds){
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("xds_id", xds.getId());
        int count = beeBaseRepository.selectCount(xds.getOdsTableName(), conditionMap);
        TaskDto taskDto = new TaskDto();
        taskDto.setXdsId(xds.getId());
        taskDto.setCountNumber(String.valueOf(count));
        taskDto.setEndTime(xds.getDataConvergeEndTime());
        return xdsInfoService.updateXdsCompleted(taskDto);
    }

    @Override
    public Xds file(DataXExecDTO dataXExecDTO, Long xdsId, String oriFilePath){
        FileConvergeInfoDTO fileConfig = new FileConvergeInfoDTO();
        BeanUtil.copyProperties(dataXExecDTO, fileConfig);
        fileConfig.setOriFilePath(oriFilePath);
        fileConfig.setOriFileType("json");
        fileConfig.setOriFileName(String.valueOf(xdsId));
        return fileService.flinkFileConverge(fileConfig, xdsId);
    }



}
