package com.lrhealth.data.converge.dao.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.dao.entity.ConvOdsDatasourceConfig;
import com.lrhealth.data.converge.dao.mapper.ConvOdsDatasourceConfigMapper;
import com.lrhealth.data.converge.dao.service.ConvOdsDatasourceConfigService;
import com.lrhealth.data.converge.model.dto.DataSourceInfoDto;
import com.lrhealth.data.converge.model.dto.DataSourceParamDto;
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
    public List<DataSourceInfoDto> getOrgReaderSource(DataSourceParamDto dto) {
        List<ConvOdsDatasourceConfig> datasourceConfigs = this.list(new LambdaQueryWrapper<ConvOdsDatasourceConfig>()
                .eq(CharSequenceUtil.isNotBlank(dto.getOrgCode()), ConvOdsDatasourceConfig::getOrgCode, dto.getOrgCode())
                .eq(dto.getDsConfigId() != null, ConvOdsDatasourceConfig::getId, dto.getDsConfigId()));
        List<DataSourceInfoDto> dataSourceInfoDtoList = new ArrayList<>();
        for (ConvOdsDatasourceConfig datasourceConfig : datasourceConfigs){
            DataSourceInfoDto sourceInfoDto = DataSourceInfoDto.builder()
                    .dsConfId(datasourceConfig.getId())
                    .orgCode(datasourceConfig.getOrgCode())
                    .sysCode(dto.getSysCode())
                    .dbType(datasourceConfig.getDbType())
                    .jdbcUrl(datasourceConfig.getDsUrl())
                    .username(datasourceConfig.getDsUsername())
                    .password(datasourceConfig.getDsPwd())
                    .schema(datasourceConfig.getSchema())
                    .structure(dto.getStructure())
                    .build();
            dataSourceInfoDtoList.add(sourceInfoDto);
        }
        return dataSourceInfoDtoList;
    }

}
