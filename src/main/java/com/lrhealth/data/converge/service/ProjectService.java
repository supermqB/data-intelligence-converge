package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.common.util.DsPageInfo;
import com.lrhealth.data.converge.model.ConvergeConfigDto;

/**
 * <p>
 * 任务调度-项目处理
 * </p>
 *
 * @author lr
 * @since 2023/7/21
 */
public interface ProjectService {

    /**
     * 查询汇聚配置信息-分页处理
     *
     * @param pageSize 分页-单页数据条数
     * @param pageNo   分页-当前页
     * @return 汇聚配置信息
     */
    DsPageInfo<ConvergeConfigDto> queryConvConfigPage(Integer pageSize, Integer pageNo);

    /**
     * 项目-配置绑定映射关系
     *
     * @param projectId    项目ID
     * @param convConfigId 配置信息ID
     * @return 关联关系ID
     */
    Long bindProjectAndConvConfig(String projectId, Long convConfigId);

    /**
     * 删除项目-配置绑定关系
     *
     * @param projectId 队列服务项目ID
     */
    void deleteProjectConvRelation(String projectId);

}
