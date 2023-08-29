package com.lrhealth.data.converge.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lrhealth.data.converge.dao.entity.ConvergeConfig;
import com.lrhealth.data.converge.model.DataXExecDTO;

import java.util.List;

/**
 * <p>
 * 配置信息接口
 * </p>
 *
 * @author lr
 * @since 2023/7/22
 */
public interface ConvConfigService {
    /**
     * 查询汇聚配置信息-分页处理
     *
     * @param pageSize 分页-单页数据条数
     * @param pageNo   分页-当前页
     * @return 汇聚配置信息
     */
    Page<ConvergeConfig> queryPage(Integer pageSize, Integer pageNo);

    /**
     * 查询汇聚配置信息
     *
     * @param pageSize 分页-单页数据条数
     * @param pageNo   分页-当前页
     * @return 汇聚配置信息
     */
    List<ConvergeConfig> queryListByPage(Integer pageSize, Integer pageNo);

    /**
     * 根据ID查询配置信息
     *
     * @param id 主键ID
     * @return 配置信息
     */
    ConvergeConfig queryById(Long id);

    DataXExecDTO getConfig(String projectId, String sourceId, Integer taskModel);
}
