package com.lrhealth.data.converge.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lrhealth.data.converge.dao.entity.ConvDsConfig;
import com.lrhealth.data.converge.model.dto.DataSourceInfoDto;
import com.lrhealth.data.converge.model.dto.DataSourceParamDto;

import java.util.List;

/**
 * <p>
 * ods数据源配置表 服务类
 * </p>
 *
 * @author zhouyun
 * @since 2023-12-22
 */
public interface ConvOdsDatasourceConfigService extends IService<ConvDsConfig> {

    List<DataSourceInfoDto> getOrgReaderSource(DataSourceParamDto dto);

    String getDbType(Integer convDsConfigId);
}
