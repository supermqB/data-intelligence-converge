package com.lrhealth.data.converge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.common.constant.CommonConstant;
import com.lrhealth.data.common.enums.conv.LogicDelFlagIntEnum;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.ConvergeConfig;
import com.lrhealth.data.converge.dao.entity.ProjectConvergeRelation;
import com.lrhealth.data.converge.dao.service.ProjectConvergeRelationService;
import com.lrhealth.data.converge.service.ConvConfigService;
import com.lrhealth.data.converge.service.ProjectConvergeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.text.CharSequenceUtil.format;
import static cn.hutool.core.text.CharSequenceUtil.isBlank;

/**
 * <p>
 * 项目-配置关系
 * </p>
 *
 * @author lr
 * @since 2023/7/22
 */
@Service
public class ProjectConvergeServiceImpl implements ProjectConvergeService {
    @Resource
    private ProjectConvergeRelationService relationService;
    @Resource
    private ConvConfigService configService;

    @Override
    public Long save(String projectId, Long configId) {
        if (isBlank(projectId)) {
            throw new CommonException("项目ID为空");
        }
        List<ProjectConvergeRelation> list = relationService.list(new LambdaQueryWrapper<ProjectConvergeRelation>().eq(ProjectConvergeRelation::getProjectId, projectId));
        if (list.size() > 1) {
            relationService.remove(new LambdaQueryWrapper<ProjectConvergeRelation>().eq(ProjectConvergeRelation::getProjectId, projectId));
        }
        ProjectConvergeRelation relation = build(projectId, configId);
        relationService.save(relation);
        return relation.getId();
    }

    @Override
    public ProjectConvergeRelation getByProjId(String projectId) {
        if (isBlank(projectId)) {
            throw new CommonException("项目ID为空");
        }
        List<ProjectConvergeRelation> list = relationService.list(new LambdaQueryWrapper<ProjectConvergeRelation>().eq(ProjectConvergeRelation::getProjectId, projectId));
        if (isEmpty(list)) {
            throw new CommonException(format("项目ID={}的汇聚配置不存在", projectId));
        }
        if (list.size() > 1) {
            throw new CommonException(format("项目ID={}的汇聚配置异常，存在{}条配置", projectId, list.size()));
        }
        return list.get(0);
    }

    /**
     * 构建关联关系
     *
     * @param projectId 项目id
     * @param configId  篇日志ID
     * @return 关联关系
     */
    private ProjectConvergeRelation build(String projectId, Long configId) {
        ConvergeConfig config = configService.queryById(configId);
        return ProjectConvergeRelation.builder()
                .convergeId(config.getId())
                .orgCode(config.getOrgCode())
                .sysCode(config.getSysCode())
                .delFlag(LogicDelFlagIntEnum.NONE.getCode())
                .createBy(CommonConstant.DEFAULT_USER)
                .createDate(LocalDateTime.now())
                .projectId(projectId)
                .build();
    }
}
