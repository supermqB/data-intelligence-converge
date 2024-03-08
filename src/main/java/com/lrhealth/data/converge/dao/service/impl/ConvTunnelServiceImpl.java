package com.lrhealth.data.converge.dao.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.enums.TunnelStatusEnum;
import com.lrhealth.data.converge.dao.entity.ConvOdsDatasourceConfig;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.mapper.ConvTunnelMapper;
import com.lrhealth.data.converge.dao.service.ConvOdsDatasourceConfigService;
import com.lrhealth.data.converge.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.model.dto.DataSourceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;


/**
 * <p>
 * 汇聚方式配置信息 服务实现类
 * </p>
 *
 * @author zhangguowen-generator
 * @since 2023-09-12
 */
@Slf4j
@Service
public class ConvTunnelServiceImpl extends ServiceImpl<ConvTunnelMapper, ConvTunnel> implements ConvTunnelService {

    @Resource
    private ConvOdsDatasourceConfigService datasourceConfigService;

    @Override
    public void updateTunnelStatus(Long tunnelId, TunnelStatusEnum tunnelStatusEnum) {
        boolean updated = this.updateById(ConvTunnel.builder()
                .id(tunnelId)
                .status(tunnelStatusEnum.getValue())
                .updateTime(LocalDateTime.now())
                .build());
        if (!updated){
            log.error("tunnel update fail, tunnelId: " + tunnelId);
        }
    }

    @Override
    public ConvTunnel getTunnelWithoutDelFlag(Long tunnelId) {
        return this.baseMapper.getTunnelWithOutDelFlag(tunnelId);
    }

    @Override
    public DataSourceDto getWriterDataSourceByTunnel(Long tunnelId) {
        if (tunnelId == null){
            throw new CommonException("tunnelId不能为空！");
        }
        ConvTunnel tunnel = this.getById(tunnelId);
        if (ObjectUtil.isEmpty(tunnel)){
            log.error("不存在的管道id:{}", tunnelId);
            return null;
        }
        ConvOdsDatasourceConfig datasource = datasourceConfigService.getById(tunnel.getWriterDatasourceId());
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
