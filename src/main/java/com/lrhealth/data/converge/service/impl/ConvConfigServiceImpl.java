package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.ConvergeConfig;
import com.lrhealth.data.converge.dao.entity.ProjectConvergeRelation;
import com.lrhealth.data.converge.dao.service.ConvergeConfigService;
import com.lrhealth.data.converge.dao.service.ProjectConvergeRelationService;
import com.lrhealth.data.converge.service.ConvConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.text.CharSequenceUtil.format;

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
}
