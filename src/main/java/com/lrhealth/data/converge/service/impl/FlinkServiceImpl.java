package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.lrhealth.data.converge.dao.adpter.BeeBaseRepository;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.dto.FileConvergeInfoDTO;
import com.lrhealth.data.converge.model.dto.FileExecInfoDTO;
import com.lrhealth.data.converge.model.dto.TaskDto;
import com.lrhealth.data.converge.service.FileService;
import com.lrhealth.data.converge.service.FlinkService;
import com.lrhealth.data.converge.service.XdsInfoService;
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
        int count = beeBaseRepository.selectCount(xds.getSysCode(), xds.getOdsTableName(), conditionMap);
        TaskDto taskDto = new TaskDto();
        taskDto.setXdsId(xds.getId());
        taskDto.setCountNumber(String.valueOf(count));
        taskDto.setEndTime(xds.getDataConvergeEndTime());
        return xdsInfoService.updateXdsCompleted(taskDto);
    }

    @Override
    public Xds file(FileExecInfoDTO fileExecInfoDTO, Long xdsId, String oriFilePath){
        FileConvergeInfoDTO fileConfig = new FileConvergeInfoDTO();
        BeanUtil.copyProperties(fileExecInfoDTO, fileConfig);
        fileConfig.setOriFilePath(oriFilePath);
        fileConfig.setOriFileType("json");
        fileConfig.setOriFileName(String.valueOf(xdsId));
        return fileService.flinkFileConverge(fileConfig, xdsId);
    }



}
