package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.model.dto.DataSourceDto;

/**
 * @author jinmengyu
 * @date 2023-12-26
 */
public interface TunnelService {

    DataSourceDto getDataSourceByTunnel(Long tunnelId);

    DataSourceDto getDataSourceBySys(String sysCode);
}
