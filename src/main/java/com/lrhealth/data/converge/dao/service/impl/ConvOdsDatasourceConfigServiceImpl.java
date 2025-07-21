package com.lrhealth.data.converge.dao.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.dao.entity.ConvDsConfig;
import com.lrhealth.data.converge.dao.mapper.ConvOdsDatasourceConfigMapper;
import com.lrhealth.data.converge.dao.service.ConvOdsDatasourceConfigService;
import com.lrhealth.data.converge.model.dto.DataSourceInfoDto;
import com.lrhealth.data.converge.model.dto.DataSourceParamDto;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
public class ConvOdsDatasourceConfigServiceImpl extends ServiceImpl<ConvOdsDatasourceConfigMapper, ConvDsConfig> implements ConvOdsDatasourceConfigService {

    @Override
    public List<DataSourceInfoDto> getOrgReaderSource(DataSourceParamDto dto) {
        List<ConvDsConfig> datasourceConfigs = this.list(new LambdaQueryWrapper<ConvDsConfig>()
                .eq(CharSequenceUtil.isNotBlank(dto.getOrgCode()), ConvDsConfig::getOrgCode, dto.getOrgCode())
                .eq(dto.getDsConfigId() != null, ConvDsConfig::getId, dto.getDsConfigId()));
        List<DataSourceInfoDto> dataSourceInfoDtoList = new ArrayList<>();
        for (ConvDsConfig datasourceConfig : datasourceConfigs){
            DataSourceInfoDto sourceInfoDto = DataSourceInfoDto.builder()
                    .orgCode(datasourceConfig.getOrgCode())
                    .sysCode(dto.getSysCode())
                    .dsConfId(datasourceConfig.getId())
                    .dbType(datasourceConfig.getDbType())
                    .jdbcUrl(datasourceConfig.getDsUrl())
                    .username(datasourceConfig.getDsUsername())
                    .password(datasourceConfig.getDsPwd())
                    .driverName(datasourceConfig.getDsDriverName())
                    .databaseName(datasourceConfig.getDbName())
                    .schema(datasourceConfig.getSchema())
                    .structure(dto.getStructure())
                    .driverName(datasourceConfig.getDsDriverName())
                    .build();
            dataSourceInfoDtoList.add(sourceInfoDto);
        }
        return dataSourceInfoDtoList;
    }

    @Override
    public String getDbType(Integer convDsConfigId) {
        if (convDsConfigId == null){
            log.error("dsConfigId为空值");
            return null;
        }
        ConvDsConfig datasourceConfig = this.getById(convDsConfigId);
        if (ObjectUtil.isEmpty(datasourceConfig)){
            log.error("不存在的数据源id[{}]", convDsConfigId);
            return null;
        }
        return datasourceConfig.getDbType();
    }

}
