package com.lrhealth.data.converge.service.impl;

import com.lrhealth.data.converge.dao.adpter.BeeBaseRepository;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.ConvFileInfoDto;
import com.lrhealth.data.converge.model.FepFileInfoVo;
import com.lrhealth.data.converge.model.TaskDto;
import com.lrhealth.data.converge.service.DocumentParseService;
import com.lrhealth.data.converge.service.FlinkService;
import com.lrhealth.data.converge.service.ShellService;
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
    private ShellService shellService;
    @Resource
    private DocumentParseService documentParseService;

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
    public Xds file(FepFileInfoVo fileInfoVo){
        String storedFileName = shellService.execShell(fileInfoVo);
        ConvFileInfoDto convFileInfoDto = ConvFileInfoDto.builder()
                .id(fileInfoVo.getXdsId()).oriFileName(fileInfoVo.getOriFileName())
                .oriFileFromIp(fileInfoVo.getFrontendIp()).oriFileType(fileInfoVo.getOriFileType()).storedFileName(storedFileName)
                .storedFileType(fileInfoVo.getOriFileType()).storedFilePath(fileInfoVo.getStoredFilePath()).build();
        Xds updatedFileXds = xdsInfoService.updateXdsFileInfo(convFileInfoDto);
        return documentParseService.flinkFileParseAndSave(updatedFileXds);
    }



}
