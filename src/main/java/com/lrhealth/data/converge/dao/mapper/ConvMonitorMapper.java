package com.lrhealth.data.converge.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lrhealth.data.converge.dao.entity.ConvFeNode;
import com.lrhealth.data.converge.dao.entity.ConvMonitor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 汇聚前置机监测表  Mapper 接口
 * </p>
 *
 * @author zhuanning
 * @since 2023-11-30
 */
@Mapper
public interface ConvMonitorMapper extends BaseMapper<ConvMonitor> {

    List<ConvFeNode> selectFepByMonitor();

}
