package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;
import static cn.hutool.core.text.StrPool.DOT;
import static cn.hutool.core.text.StrPool.SLASH;

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
    public void fepConverge(String projectId) {
        // 获得文件列表
        List<FepFileInfoVo> fepFileInfoVos = fileConverge(projectId);
        fepFileInfoVos.forEach(fepFileInfoVo -> {
            // 新建xds
            Xds fileXds = xdsInfoService.createFileXds(fepFileInfoVo);
            // 文件搬运
            fepFileInfoVo.setStoredFilePath(fepFileInfoVo.getStoredFilePath() + SLASH + fepFileInfoVo.getOrgCode() + SLASH +  fepFileInfoVo.getSysCode());
            fepFileInfoVo.setXdsId(fileXds.getId());
            String storedFileName = shellService.execShell(fepFileInfoVo);
            ConvFileInfoDto convFileInfoDto =  ConvFileInfoDto.builder()
                    .id(fepFileInfoVo.getXdsId()).oriFileName(fepFileInfoVo.getOriFileName()).oriFileSize(fepFileInfoVo.getOriFileSize())
                    .oriFileFromIp(fepFileInfoVo.getFrontendIp()).oriFileType(fepFileInfoVo.getOriFileType())
                    .storedFileName(storedFileName).storedFileType(fepFileInfoVo.getOriFileType())
                    .storedFilePath(fepFileInfoVo.getStoredFilePath()).build();
            // 更新xds文件信息
            Xds xds = xdsInfoService.updateXdsFileInfo(convFileInfoDto);
            // 数据解析落库
            documentParseService.fileParseAndSave(xds.getId());
            // 发送kafka
            xdsSendKafka(xds);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Xds flinkConverge(FlinkTaskDto dto) {
        Xds result = null;
        ConvergeConfig config = configService.getOne(new LambdaQueryWrapper<ConvergeConfig>().eq(isNotBlank(dto.getSourceId()), ConvergeConfig::getSysCode, dto.getSourceId()));
        if (ObjectUtil.isEmpty(config)){
            throw new CommonException("flink关联配置为空");
        }
        Xds flinkXds = xdsInfoService.createFlinkXds(dto, config);
        if (FlinkTypeEnum.isDataBase(dto.getType())){
            result = flinkService.database(flinkXds);
        }else if (FlinkTypeEnum.isFile(dto.getType())){
            FepFileInfoVo fileInfoVo = getFrontendRelation(config);
            String filePath = CharSequenceUtil.isBlank(dto.getFilePath()) ? null : dto.getFilePath().substring(0, dto.getFilePath().length() - String.valueOf(dto.getXdsId()).length() -1);
            fileInfoVo.setOriFileFromPath(filePath);
            fileInfoVo.setOriFileType("json");
            fileInfoVo.setOriFileName(String.valueOf(dto.getXdsId()));
            fileInfoVo.setStoredFilePath(fileInfoVo.getStoredFilePath() + SLASH + flinkXds.getOrgCode() + SLASH + flinkXds.getSysCode());
            fileInfoVo.setXdsId(flinkXds.getId());
            result = flinkService.file(fileInfoVo);
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
        // 项目配置项，以及与前置机的关联
        ConvergeConfig convergeConfig = getConfig(projectId);
        FepFileInfoVo frontendRelation = getFrontendRelation(convergeConfig);
        // 调用前置机接口
        List<FileInfo> fepFileList = fepService.fepFileList(frontendRelation.getOriFileFromPath());
        List<FepFileInfoVo> fepFileInfoVos = new ArrayList<>();
        fepFileList.forEach(fileInfo -> {
            FepFileInfoVo fepFileInfoVo = new FepFileInfoVo();
            BeanUtil.copyProperties(frontendRelation, fepFileInfoVo);
            fepFileInfoVo.setOriFileName(fileInfo.getFileName());
            fepFileInfoVo.setOriFileType(fileInfo.getFileName().substring(fileInfo.getFileName().lastIndexOf(DOT) + 1));
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
    private FepFileInfoVo getFrontendRelation(ConvergeConfig convergeConfig){
        Frontend frontendConfig = frontendService.getByFrontenfCode(convergeConfig.getFrontendCode());
        return FepFileInfoVo.builder().frontendIp(frontendConfig.getFrontendIp())
                .frontendPort(frontendConfig.getFrontendPort()).frontendPwd(frontendConfig.getFrontendPwd())
                .frontendUsername(frontendConfig.getFrontendUsername()).encryptionWay(convergeConfig.getEncryptionWay())
                .zipFlag(convergeConfig.getZipFlag()).oriFileFromPath(frontendConfig.getFilePath())
                .storedFilePath(convergeConfig.getStoredFilePath()).sysCode(convergeConfig.getSysCode()).orgCode(convergeConfig.getOrgCode())
                .convergeMethod(convergeConfig.getConvergeMethod()).dataType(convergeConfig.getDataType()).build();
    }

}
