package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.ConvOdsDatasourceConfig;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.service.ConvOdsDatasourceConfigService;
import com.lrhealth.data.converge.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.model.dto.DataSourceDto;
import com.lrhealth.data.converge.service.TunnelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author jinmengyu
 * @date 2023-12-26
 */
@Slf4j
@Service
public class TunnelServiceImpl implements TunnelService {
    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private ConvOdsDatasourceConfigService datasourceConfigService;


    @Override
    public DataSourceDto getDataSourceByTunnel(Long tunnelId) {
        if (tunnelId == null){
            throw new CommonException("tunnelId不能为空！");
        }
        ConvTunnel tunnel = tunnelService.getById(tunnelId);
        if (ObjectUtil.isEmpty(tunnel)){
            log.error("不存在的管道id:{}", tunnelId);
            return null;
        }
        ConvOdsDatasourceConfig datasource = datasourceConfigService.getById(tunnel.getJdbcUrl());
        if (ObjectUtil.isEmpty(datasource)){
            log.error("不存在的数据源配置id:{}", tunnelId);
            return null;
        }
        return DataSourceDto.builder()
                .jdbcUrl(datasource.getDsUrl())
                .username(datasource.getDsUsername())
                .password(datasource.getDsPwd())
                .driver(datasource.getDsDriverName())
                .build();
    }

    @Override
    public DataSourceDto getDataSourceBySys(String sysCode) {
        if (CharSequenceUtil.isBlank(sysCode)){
            throw new CommonException("sysCode不能为空！");
        }
        ConvOdsDatasourceConfig datasource = datasourceConfigService.getOne(new LambdaQueryWrapper<ConvOdsDatasourceConfig>()
                .eq(ConvOdsDatasourceConfig::getSysCode, sysCode)
                .eq(ConvOdsDatasourceConfig::getDsType, 1));
        if (ObjectUtil.isEmpty(datasource)){
            log.error("不存在的数据源系统配置:{}", sysCode);
            return null;
        }
        return DataSourceDto.builder()
                .jdbcUrl(datasource.getDsUrl())
                .username(datasource.getDsUsername())
                .password(datasource.getDsPwd())
                .driver(datasource.getDsDriverName())
                .build();
    }
}
