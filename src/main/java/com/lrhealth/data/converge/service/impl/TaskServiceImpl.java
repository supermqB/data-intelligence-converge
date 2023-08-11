package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.lrhealth.data.common.enums.conv.FlinkTypeEnum;
import com.lrhealth.data.common.enums.conv.KafkaSendFlagEnum;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.ConvergeConfig;
import com.lrhealth.data.converge.dao.entity.Frontend;
import com.lrhealth.data.converge.dao.entity.ProjectConvergeRelation;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.dao.service.ConvergeConfigService;
import com.lrhealth.data.converge.dao.service.FrontendService;
import com.lrhealth.data.converge.model.*;
import com.lrhealth.data.converge.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Slf4j
public class TaskServiceImpl implements TaskService {
    @Value("${spring.kafka.topic.xds}")
    private String topic;
    @Value("${converge.filepath}")
    private String storedFilePath;
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
    @Resource
    private FlinkService flinkService;
    @Resource
    private ShellService shellService;

    @Override
    public Xds createTask(@RequestBody TaskDto taskDto) {
        ConvergeConfig config = getConfig(taskDto.getProjectId());
        return xdsInfoService.createXdsInfo(taskDto, config);
    }

    @Override
    public Xds updateTask(TaskDto taskDto) {
        Xds xds;
        //TODO: 目前任务状态在调度中写死TRUE,后续应该通过读取datax配置得到数据抽取结果
        if (taskDto.isTaskStatus()) {
            xds = xdsInfoService.updateXdsCompleted(taskDto);
            xdsSendKafka(xds);
        } else {
            xds = xdsInfoService.updateXdsFailure(taskDto.getXdsId(), "dataX数据抽取失败");
        }
        return xds;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void localFileParse(String projectId) {
        List<FepFileInfoVo> fepFileInfoVos = fileConverge(projectId);
        fepFileInfoVos.forEach(fepFileInfoVo -> {
            Xds fileXds = xdsInfoService.createFileXds(projectId, fepFileInfoVo);
            fileXds.setStoredFilePath(storedFilePath + "/" + fepFileInfoVo.getOrgCode() + "/" +  fepFileInfoVo.getSysCode());
            String storedFileName = shellService.execShell(fileXds);
            ConvFileInfoDto convFileInfoDto = ConvFileInfoDto.builder()
                    .id(fileXds.getId()).storedFileName(storedFileName)
                    .storedFileType(fepFileInfoVo.getOriFileType())
                    .storedFilePath(fileXds.getStoredFilePath()).build();
            Xds xds = xdsInfoService.updateXdsFileInfo(convFileInfoDto);
            documentParseService.fileParseAndSave(xds.getId());
            xdsSendKafka(xds);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Xds flinkConverge(FlinkTaskDto flinkTaskDto) {
        Xds result = null;
        Xds flinkXds = xdsInfoService.createFlinkXds(flinkTaskDto);
        if (FlinkTypeEnum.isDataBase(flinkTaskDto.getType())){
            result = flinkService.database(flinkXds);
        }else if (FlinkTypeEnum.isFile(flinkTaskDto.getType())){
            flinkXds.setStoredFilePath(storedFilePath + "/" + flinkXds.getOrgCode() + "/" + flinkXds.getSysCode());
            result =  flinkService.file(flinkXds);
        }
        xdsSendKafka(result == null ? new Xds() : result);
        return result;
    }


    /**
     * 组装文件与配置信息
     * @param projectId 项目ID
     * @return 前置机文件及信息
     */
    private List<FepFileInfoVo> fileConverge(String projectId) {
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


    /**
     * 发送kafka消息
     *
     * @param xds XDS信息
     */
    private void xdsSendKafka(Xds xds) {
        if (ObjectUtil.isNull(xds)){
            throw new CommonException("kafka send xds is null");
        }
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

}
