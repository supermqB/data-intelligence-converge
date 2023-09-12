package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lrhealth.data.common.enums.conv.ConvModeEnum;
import com.lrhealth.data.common.enums.conv.ConvergeTypeEnum;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.ConvergeConfig;
import com.lrhealth.data.converge.dao.entity.Frontend;
import com.lrhealth.data.converge.dao.entity.ProjectConvergeRelation;
import com.lrhealth.data.converge.dao.service.ConvergeConfigService;
import com.lrhealth.data.converge.dao.service.FrontendService;
import com.lrhealth.data.converge.dao.service.ProjectConvergeRelationService;
import com.lrhealth.data.converge.model.FileExecInfoDTO;
import com.lrhealth.data.converge.service.ConvConfigService;
import com.lrhealth.data.converge.service.ProjectConvergeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.text.CharSequenceUtil.format;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;

;

/**
 * <p>
 * 配置信息接口实现类
 * </p>
 *
 * @author lr
 * @since 2023/7/22
 */
@Service
public class ConvConfigServiceImpl implements ConvConfigService {
    @Resource
    private ConvergeConfigService configService;
    @Resource
    private ProjectConvergeRelationService relationService;
    @Resource
    private ProjectConvergeService proConvService;
    @Resource
    FrontendService frontendService;

    @Override
    public Page<ConvergeConfig> queryPage(Integer pageSize, Integer pageNo) {
        Page<ConvergeConfig> page = new Page<>(pageNo, pageSize);
        return configService.page(page);
    }

    @Override
    public List<ConvergeConfig> queryListByPage(Integer pageSize, Integer pageNo) {
        List<ConvergeConfig> convergeConfigs = configService.list();
        List<Long> bindedConvergeConfigs = relationService.list().stream().distinct().map(ProjectConvergeRelation::getConvergeId).collect(Collectors.toList());
        List<ConvergeConfig> filteredConfigs = convergeConfigs.stream().filter(convergeConfig -> !bindedConvergeConfigs.contains(convergeConfig.getId())).collect(Collectors.toList());
        return isEmpty(filteredConfigs) ? CollUtil.newArrayList() : filteredConfigs;
    }

    @Override
    public ConvergeConfig queryById(Long id) {
        if (null == id) {
            throw new CommonException("配置信息ID为空");
        }
        ConvergeConfig config = configService.getById(id);
        if (null == config) {
            throw new CommonException(format("配置信息ID={}的配置信息不存在"));
        }
        return config;
    }

    @Override
    public FileExecInfoDTO getConfig(String projectId, String sourceId, Integer taskModel) {
        ConvergeConfig baseConfig;
        // 任务调度中项目与配置的关联
        if (CharSequenceUtil.isNotBlank(projectId)){
            ProjectConvergeRelation relation = proConvService.getByProjId(projectId);
            baseConfig = configService.getById(relation.getConvergeId());
        }else {
            baseConfig = configService.getOne(new LambdaQueryWrapper<ConvergeConfig>()
                    .eq(isNotBlank(sourceId), ConvergeConfig::getSysCode, sourceId));
        }
        if (ObjectUtil.isNotNull(baseConfig)){
            return convergeMode(baseConfig, taskModel);
        }
        return new FileExecInfoDTO();
    }

    private FileExecInfoDTO convergeMode(ConvergeConfig baseConfig, Integer taskModel){
        // 前置机模式
        if (ConvModeEnum.isFrontend(baseConfig.getConvergeMode())){
            return frontendConfig(baseConfig, taskModel);
        }
        // 直连模式
        return directConnectConfig(baseConfig, taskModel);
    }


    private FileExecInfoDTO frontendConfig(ConvergeConfig config, Integer taskModel){
        FileExecInfoDTO dataXExecBO;
        Frontend frontend = frontendService.getByFrontendCode(config.getFrontendCode());
        // db
        dataXExecBO = FileExecInfoDTO.builder()
                .orgCode(config.getOrgCode()).sysCode(config.getSysCode()).convergeMethod(config.getConvergeMethod())
                .frontendIp(frontend.getFrontendIp()).frontendPort(frontend.getFrontendPort())
                .frontendUsername(frontend.getFrontendUsername()).frontendPwd(frontend.getFrontendPwd())
                .build();
        // file
        if (ConvergeTypeEnum.isFile(taskModel)){
            // 前置机目录/dataX生成的文件目录
            dataXExecBO.setOriFilePath(frontend.getFilePath());
            dataXExecBO.setStoredFilePath(config.getStoredFilePath());
        }
        return dataXExecBO;
    }

    private FileExecInfoDTO directConnectConfig(ConvergeConfig config, Integer taskModel){
        FileExecInfoDTO dataXExecBO;
        dataXExecBO = FileExecInfoDTO.builder()
                .orgCode(config.getOrgCode()).sysCode(config.getSysCode())
                .convergeMethod(config.getConvergeMethod()).build();
        if (ConvergeTypeEnum.isFile(taskModel)){
            // 直连模式，dataX生成文件的目录就是汇聚读取的目录
            dataXExecBO.setOriFilePath(config.getStoredFilePath());
            dataXExecBO.setStoredFilePath(config.getStoredFilePath());
        }
        return dataXExecBO;
    }

}
