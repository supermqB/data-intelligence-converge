package com.lrhealth.data.converge.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lrhealth.data.converge.dao.entity.ConvOdsDatasourceConfig;

/**
 * <p>
 * ods数据源配置表 Mapper 接口
 * </p>
 *
 * @author zhouyun
 * @since 2023-12-22
 */
public interface ConvOdsDatasourceConfigMapper extends BaseMapper<ConvOdsDatasourceConfig> {

    void updateTime(Boolean status,Integer dsId);
}
