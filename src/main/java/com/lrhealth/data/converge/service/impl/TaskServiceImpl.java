package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.lrhealth.data.common.enums.conv.KafkaSendFlagEnum;
import com.lrhealth.data.converge.common.util.ShellUtil;
import com.lrhealth.data.converge.dao.entity.ConvergeConfig;
import com.lrhealth.data.converge.dao.entity.Frontend;
import com.lrhealth.data.converge.dao.entity.ProjectConvergeRelation;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.dao.service.ConvergeConfigService;
import com.lrhealth.data.converge.dao.service.FrontendService;
import com.lrhealth.data.converge.model.*;
import com.lrhealth.data.converge.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 任务接口实现类
 * </p>
 *
 * @author lr
 * @since 2023/7/19
 */
@Service
public class TaskServiceImpl implements TaskService {
    @Value("${spring.kafka.topic.xds}")
    private String topic;
    @Resource
    private XdsInfoService xdsInfoService;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;
    @Resource
    private ProjectConvergeService proConvService;
    @Resource
    private ConvergeConfigService configService;
    @Resource
    private FepService fepService;

    @Resource
    private FrontendService frontendService;

    @Resource
    private DocumentParseService documentParseService;

    @Override
    public Xds createTask(@RequestBody TaskDto taskDto) {
        ConvergeConfig config = getConfig(taskDto.getProjectId());
        return xdsInfoService.createXdsInfo(taskDto, config);
    }

    @Override
    public Xds updateTask(TaskDto taskDto) {
        Xds xds;
        if (taskDto.isTaskStatus()) {
            xds = xdsInfoService.updateXdsCompleted(taskDto);
            xdsSendKafka(xds);
        } else {
            xds = xdsInfoService.updateXdsFailure(taskDto.getXdsId(), "dataX数据抽取失败");
        }
        return xds;
    }

    @Override
    public List<FepFileInfoVo> fileConverge(String projectId) {
        FepFileInfoVo frontendRelation = getFrontendRelation(projectId);
        List<FileInfo> fepFileList = fepService.getFepFileList(frontendRelation.getOriFileFromPath());
        List<FepFileInfoVo> fepFileInfoVos = new ArrayList<>();
        fepFileList.forEach(fileInfo -> {
            FepFileInfoVo fepFileInfoVo = new FepFileInfoVo();
            BeanUtil.copyProperties(frontendRelation, fepFileInfoVo);
            fepFileInfoVo.setOriFileName(fileInfo.getFileName());
            fepFileInfoVo.setOriFileType(fileInfo.getFileName().substring(fileInfo.getFileName().lastIndexOf(".") + 1));
            fepFileInfoVo.setOriFileSize(BigDecimal.valueOf(fileInfo.getFileSize()));
            fepFileInfoVos.add(fepFileInfoVo);
        });
        return fepFileInfoVos;
    }

    @Override
    public void localFileParse(String projectId) {
        List<FepFileInfoVo> fepFileInfoVos = fileConverge(projectId);
        fepFileInfoVos.forEach(fepFileInfoVo -> {
            Xds fileXds = xdsInfoService.createFileXds(projectId, fepFileInfoVo);
            fileXds.setStoredFilePath(fepFileInfoVo.getStoredFilePath()
                    + "/" + fepFileInfoVo.getOrgCode() + "/" +  fepFileInfoVo.getSysCode());
            String storedFileName = execShell(fepFileInfoVo, fileXds);
            ConvFileInfoDto convFileInfoDto = ConvFileInfoDto.builder()
                    .oriFileName(fepFileInfoVo.getOriFileName()).oriFileFromIp(fepFileInfoVo.getFrontendIp())
                    .id(fileXds.getId()).storedFileName(storedFileName)
                    .storedFilePath(fileXds.getStoredFilePath())
                    .storedFileType(fepFileInfoVo.getOriFileType())
                    .oriFileType(fepFileInfoVo.getOriFileType()).build();
            Xds xds = xdsInfoService.updateXdsFileInfo(convFileInfoDto);
            documentParseService.documentParseAndSave(xds.getId());
            xdsSendKafka(xds);
        });
    }

    @Override
    public Xds flinkCreateXds(FlinkTaskDto flinkTaskDto) {
        ConvergeConfig config = configService.getById(flinkTaskDto.getConvergeConfigId());
        return xdsInfoService.createFlinkXds(config, flinkTaskDto);
    }


    /**
     * 发送kafka消息
     *
     * @param xds XDS信息
     */
    private void xdsSendKafka(Xds xds) {
        Xds checkXds = xdsInfoService.getById(xds.getId());
        if (!KafkaSendFlagEnum.isSent(xds.getKafkaSendFlag())) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", checkXds.getId());
            kafkaTemplate.send(topic, JSON.toJSONString(map));
            xdsInfoService.updateKafkaSent(xds);
        }
    }

    /**
     * 获取配置信息
     *
     * @param projId 项目ID
     * @return 配置信息
     */
    private ConvergeConfig getConfig(String projId) {
        ProjectConvergeRelation relation = proConvService.getByProjId(projId);
        return configService.getById(relation.getConvergeId());
    }

    /**
     * 关联前置机和配置信息
     */
    private FepFileInfoVo getFrontendRelation(String projId){
        ConvergeConfig convergeConfig = getConfig(projId);
        Frontend frontendConfig = frontendService.getByFrontenfCode(convergeConfig.getFrontendCode());
        return FepFileInfoVo.builder().frontendIp(frontendConfig.getFrontendIp())
                .frontendPort(frontendConfig.getFrontendPort()).frontendPwd(frontendConfig.getFrontendPwd())
                .frontendUsername(frontendConfig.getFrontendUsername()).encryptionWay(convergeConfig.getEncryptionWay())
                .zipFlag(convergeConfig.getZipFlag()).oriFileFromPath(frontendConfig.getFilePath())
                .storedFilePath(convergeConfig.getStoredFilePath()).sysCode(convergeConfig.getSysCode()).orgCode(convergeConfig.getOrgCode())
                .convergeMethod(convergeConfig.getConvergeMethod()).dataType(convergeConfig.getDataType()).build();
    }

    private String execShell(FepFileInfoVo fepFileInfoVo, Xds fileXds){
        List<String> command = new ArrayList<>();
        String storedFileName = fileXds.getId() + "." + fepFileInfoVo.getOriFileType();
        command.add("cp");
        command.add(fepFileInfoVo.getOriFileFromPath() + "/" + fepFileInfoVo.getOriFileName());
        command.add(fileXds.getStoredFilePath() + "/" + storedFileName);
        ShellUtil.execCommand(command);
        List<String> mvCommand = new ArrayList<>();
        mvCommand.add("mv");
        mvCommand.add(fepFileInfoVo.getOriFileFromPath() + "/" + fepFileInfoVo.getOriFileName());
        mvCommand.add("/tmp/file/backup");
        ShellUtil.execCommand(mvCommand);
        return storedFileName;
    }

}
