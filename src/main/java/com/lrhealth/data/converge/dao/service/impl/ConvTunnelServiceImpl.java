package com.lrhealth.data.converge.dao.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.common.enums.TunnelStatusEnum;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.mapper.ConvTunnelMapper;
import com.lrhealth.data.converge.dao.service.ConvTunnelService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


/**
 * <p>
 * 汇聚方式配置信息 服务实现类
 * </p>
 *
 * @author zhangguowen-generator
 * @since 2023-09-12
 */
@Service
public class ConvTunnelServiceImpl extends ServiceImpl<ConvTunnelMapper, ConvTunnel> implements ConvTunnelService {

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

}
