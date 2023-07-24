package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lrhealth.data.converge.common.util.DsPageInfo;
import com.lrhealth.data.converge.dao.entity.ConvergeConfig;
import com.lrhealth.data.converge.dao.entity.Institution;
import com.lrhealth.data.converge.dao.entity.System;
import com.lrhealth.data.converge.model.ConvergeConfigDto;
import com.lrhealth.data.converge.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollUtil.isEmpty;

/**
 * <p>
 * 任务调度-项目处理
 * </p>
 *
 * @author lr
 * @since 2023/7/21
 */
@Service
public class ProjectServiceImpl implements ProjectService {
    @Resource
    private ConvConfigService configService;
    @Resource
    private OrgService orgService;
    @Resource
    private SysService sysService;
    @Resource
    private ProjectConvergeService projectConvergeService;

    @Override
    public DsPageInfo<ConvergeConfigDto> queryConvConfigPage(Integer pageSize, Integer pageNo) {
        DsPageInfo<ConvergeConfigDto> pageInfo = new DsPageInfo<>(pageNo, pageSize);
        Page<ConvergeConfig> configPage = configService.queryPage(pageSize, pageNo);

        pageInfo.setTotal((int) configPage.getTotal());
        pageInfo.setTotalList(buildResultList(configPage.getRecords()));

        return pageInfo;
    }

    @Override
    public Long bindProjectAndConvConfig( String projectId,Long convConfigId) {
        return projectConvergeService.save(projectId,convConfigId);
    }

    /**
     * 构造接口结果
     *
     * @param configList 配置信息集合
     * @return 接口结果
     */
    private List<ConvergeConfigDto> buildResultList(List<ConvergeConfig> configList) {
        // 获取机构编码
        List<String> orgCodeList = getOrgCodeList(configList);
        // 获取机构信息
        Map<String, Institution> orgMap = orgService.queryOrgMap(orgCodeList);
        // 获取系统信息
        Map<String, System> systemMap = sysService.querySysMapByOrgCode(orgCodeList);
        return configList.stream().map(
                item -> buildDto(item, orgMap.get(item.getOrgCode()).getOrgAffiliationName(), systemMap.get(item.getSysCode()).getSystemName())
        ).collect(Collectors.toList());
    }

    /**
     * 构造接口返回结果
     *
     * @param config  配置信息
     * @param orgName 机构名称
     * @param sysName 系统名称
     * @return 接口返回结果
     */
    private ConvergeConfigDto buildDto(ConvergeConfig config, String orgName, String sysName) {
        return ConvergeConfigDto.builder()
                .convergeId(config.getId())
                .convergeName(config.getConvergeName())
                .orgName(orgName)
                .orgCode(config.getOrgCode())
                .sysCode(config.getSysCode())
                .sysName(sysName)
                .build();
    }

    /**
     * 获取机构编码集合
     *
     * @param configList 汇聚配置信息集合
     * @return 机构编码集合
     */
    private List<String> getOrgCodeList(List<ConvergeConfig> configList) {
        return isEmpty(configList) ? CollUtil.newArrayList() : configList.stream().map(ConvergeConfig::getOrgCode).collect(Collectors.toList());
    }
}
