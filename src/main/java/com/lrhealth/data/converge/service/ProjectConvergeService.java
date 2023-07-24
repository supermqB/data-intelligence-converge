package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.ProjectConvergeRelation;

/**
 * <p>
 * 项目-配置关系
 * </p>
 *
 * @author lr
 * @since 2023/7/22
 */
public interface ProjectConvergeService {

    /**
     * 保存映射关系
     *
     * @param projectId 项目ID
     * @param configId  配置ID
     * @return 关联关系ID
     */
    Long save(String projectId, Long configId);

    /**
     * 根据项目ID查询关联选系
     *
     * @param projectId 项目ID
     * @return 关联关系
     */
    ProjectConvergeRelation getByProjId(String projectId);
}
