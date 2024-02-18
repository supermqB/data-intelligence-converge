package com.lrhealth.data.converge.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 汇聚方式配置信息 Mapper 接口
 * </p>
 *
 * @author zhangguowen-generator
 * @since 2023-09-12
 */
@Mapper
public interface ConvTunnelMapper extends BaseMapper<ConvTunnel> {

    ConvTunnel getTunnelWithOutDelFlag(@Param("tunnelId") Long tunnelId);

}
