package com.lrhealth.data.converge.dao.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.model.dto.DataSourceDto;


/**
 * <p>
 * 汇聚方式配置信息 服务类
 * </p>
 *
 * @author zhangguowen-generator
 * @since 2023-09-12
 */
public interface ConvTunnelService extends IService<ConvTunnel> {

    ConvTunnel getTunnelWithoutDelFlag(Long tunnelId);

    DataSourceDto getWriterDataSourceByTunnel(Long tunnelId);

    DataSourceDto getDataSourceBySys(String sysCode);
}
