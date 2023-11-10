package com.lrhealth.data.converge.scheduled.dao.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.lrhealth.data.converge.common.enums.TunnelStatusEnum;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;


/**
 * <p>
 * 汇聚方式配置信息 服务类
 * </p>
 *
 * @author zhangguowen-generator
 * @since 2023-09-12
 */
public interface ConvTunnelService extends IService<ConvTunnel> {

    /**
     * 更新管道状态
     * @param tunnelId    管道id
     * @param tunnelStatusEnum  管道状态
     */
    void updateTunnelStatus(Long tunnelId, TunnelStatusEnum tunnelStatusEnum);
}
