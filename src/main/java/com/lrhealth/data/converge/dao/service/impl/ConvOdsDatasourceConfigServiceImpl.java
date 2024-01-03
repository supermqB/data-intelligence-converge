package com.lrhealth.data.converge.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.dao.entity.ConvOdsDatasourceConfig;
import com.lrhealth.data.converge.dao.mapper.ConvOdsDatasourceConfigMapper;
import com.lrhealth.data.converge.dao.service.ConvOdsDatasourceConfigService;
import com.lrhealth.data.converge.model.dto.DataSourceInfoDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * ods数据源配置表 服务实现类
 * </p>
 *
 * @author zhouyun
 * @since 2023-12-22
 */
@Service
public class ConvOdsDatasourceConfigServiceImpl extends ServiceImpl<ConvOdsDatasourceConfigMapper, ConvOdsDatasourceConfig> implements ConvOdsDatasourceConfigService {

    @Override
    public List<DataSourceInfoDto> getOrgReaderSource(String orgCode) {
        List<ConvOdsDatasourceConfig> datasourceConfigs = this.list(new LambdaQueryWrapper<ConvOdsDatasourceConfig>()
                .eq(ConvOdsDatasourceConfig::getOrgCode, orgCode)
                .eq(ConvOdsDatasourceConfig::getDsType, 2));
        List<DataSourceInfoDto> dataSourceDtoList = new ArrayList<>();
        datasourceConfigs.forEach(datasourceConfig -> {
            DataSourceInfoDto sourceInfoDto = DataSourceInfoDto.builder()
                    .orgCode(datasourceConfig.getOrgCode())
                    .sysCode(datasourceConfig.getSysCode())
                    .jdbcUrl(datasourceConfig.getDsUrl())
                    .username(datasourceConfig.getDsUsername())
                    .password(datasourceConfig.getDsPwd())
                    .build();
            dataSourceDtoList.add(sourceInfoDto);
        });
        return dataSourceDtoList;
    }

}
