package com.lrhealth.data.converge.dao.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.cache.Cache;
import com.lrhealth.data.converge.common.util.StringUtils;
import com.lrhealth.data.converge.dao.entity.ConvMonitor;
import com.lrhealth.data.converge.dao.mapper.ConvMonitorMapper;
import com.lrhealth.data.converge.dao.service.ConvMonitorService;
import com.lrhealth.data.converge.model.dto.MonitorMsg;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvFeNode;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.dao.mapper.DiConvFeNodeMapper;
import com.lrhealth.data.converge.scheduled.dao.mapper.DiConvTunnelMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author admin
 */
@Slf4j
@Service
public class ConvMonitorServiceImpl implements ConvMonitorService {
    @Resource
    private DiConvFeNodeMapper diConvFeNodeMapper;
    @Resource
    private ConvMonitorMapper convMonitorMapper;
    @Resource
    private DiConvTunnelMapper convTunnelMapper;
    @Resource
    private Cache convCache;

    private static final String CONCAT_STR = "*";

    private static final String FE_NODE_PREFIX = "fe_node:";

    private static final String CONV_MONITOR = "conv_monitor:";


    @Async
    @Override
    public void handleMonitorMsg(MonitorMsg message) {
        //查询缓存中前置机信息
        ConvFeNode feNode;
        String feNodeCacheKey;
        if (MonitorMsg.MsgTypeEnum.FEP_STA.getMsgTypeCode().equals(message.getMsgType())
                || MonitorMsg.MsgTypeEnum.CDC_STA.getMsgTypeCode().equals(message.getMsgType())) {
            feNodeCacheKey = FE_NODE_PREFIX.concat(message.getSourceIp())
                    .concat(CONCAT_STR).concat(message.getSourcePort())
                    .concat(CONCAT_STR).concat(message.getOrgCode())
                    .concat(CONCAT_STR).concat(message.getMsgType());
        } else {
            feNodeCacheKey = FE_NODE_PREFIX.concat(message.getSourceIp())
                    .concat(CONCAT_STR).concat(message.getSourcePort())
                    .concat(CONCAT_STR).concat(String.valueOf(message.getTunnelId()))
                    .concat(CONCAT_STR).concat(message.getMsgType());
        }
        Object cacheValue = convCache.getObject(feNodeCacheKey);
        if (cacheValue == null) {
            feNode = queryFeNodeFromDatabase(message, feNodeCacheKey);
        } else {
            feNode = JSON.parseObject(JSON.toJSONString(cacheValue), ConvFeNode.class);
        }
        //消息处理
        if (Objects.isNull(feNode)) {
            log.info("未查询到前置机信息 message = {}", message);
            return;
        }
        processConvMonitor(feNode, message);
        setConvFeNodeProperties(feNode, message);
        //更新缓存中前置机信息
        convCache.putObject(feNodeCacheKey, feNode);
    }

    /**
     * 根据监测修改缓存前置机状态
     */
    private void setConvFeNodeProperties(ConvFeNode convFeNode, MonitorMsg message) {
        convFeNode.setUpdateTime(message.getSendTime());
        convFeNode.setState(StringUtils.isNotEmpty(message.getMsg()) ? 0 : 1);
    }


    /**
     * 处理汇聚监视器信息
     *
     * @param convFeNode 前置机
     * @param message    监测消息
     */
    @Override
    public void processConvMonitor(ConvFeNode convFeNode, MonitorMsg message) {
        //查询缓存中监视器信息
        Object monitorCache = convCache.getObject(CONV_MONITOR + convFeNode.getId() + message.getMsgType());
        ConvMonitor monitor;
        ConvMonitor convMonitor;
        if (monitorCache == null) {
            convMonitor = convMonitorMapper.selectOne(new LambdaQueryWrapper<ConvMonitor>().eq(ConvMonitor::getConvFeNodeId, convFeNode.getId())
                    .eq(ConvMonitor::getMonitorType, message.getMsgType()));
        } else {
            convMonitor = JSON.parseObject(JSON.toJSONString(monitorCache), ConvMonitor.class);
        }
        monitor = buildConvMonitor(convMonitor, message, convFeNode);
        int currentState = message.getStatus() ? 0 : 1;
        //有异常 或 状态变更时 会更新库和缓存
        if (StringUtils.isNotEmpty(message.getMsg()) || currentState != convFeNode.getState()) {
            if (convMonitor == null) {
                convMonitorMapper.insert(monitor);
            } else {
                convMonitorMapper.updateById(monitor);
            }
        }
        //更新monitor缓存
        convCache.putObject(CONV_MONITOR + convFeNode.getId() + message.getMsgType(), monitor);
    }

    /**
     * 数据库查询前置机信息
     */
    private ConvFeNode queryFeNodeFromDatabase(MonitorMsg message, String feNodeCacheKey) {
        LambdaQueryWrapper<ConvFeNode> queryWrapper = new LambdaQueryWrapper<ConvFeNode>()
                .eq(ConvFeNode::getIp, message.getSourceIp())
                .eq(ConvFeNode::getPort, Integer.parseInt(message.getSourcePort()));
        //管道监控
        if (message.getTunnelId() != null) {
            ConvTunnel convTunnel = convTunnelMapper.selectOne(new LambdaQueryWrapper<ConvTunnel>()
                    .eq(ConvTunnel::getId, message.getTunnelId()));
            if (convTunnel == null || convTunnel.getFrontendId() < 0) {
                log.info("<----前置机不存在,采用库到库直连方式---->");
                return null;
            }
            queryWrapper.eq(ConvFeNode::getId, convTunnel.getFrontendId());
        } else {//服务状态监控
            queryWrapper.eq(ConvFeNode::getOrgCode, message.getOrgCode());
        }
        List<ConvFeNode> feNodeList = diConvFeNodeMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(feNodeList)) {
            convCache.putObject(feNodeCacheKey, feNodeList.get(0));
            return feNodeList.get(0);
        }
        return null;
    }

    /**
     * 构建汇聚前置机监测信息
     *
     * @param convMonitor 监测信息
     * @param message     监测消息
     * @param convFeNode  前置机信息
     * @return 监测信息
     */
    private ConvMonitor buildConvMonitor(ConvMonitor convMonitor, MonitorMsg message, ConvFeNode convFeNode) {
        ConvMonitor monitor = new ConvMonitor();
        if (convMonitor == null) {
            monitor.setConvFeNodeId(convFeNode.getId());
            monitor.setSysCode(convFeNode.getSysCode());
            monitor.setOrgCode(convFeNode.getOrgCode());
        } else {
            monitor.setId(convMonitor.getId());
            monitor.setConvFeNodeId(convMonitor.getConvFeNodeId());
            monitor.setSysCode(convMonitor.getSysCode());
            monitor.setOrgCode(convMonitor.getOrgCode());
        }
        if (message.getSendTime() != null) {
            monitor.setExceptionTime(message.getSendTime());
        }
        if (message.getMsgType() != null) {
            monitor.setMonitorType(message.getMsgType());
        }
        monitor.setExceptionDes(message.getMsg());
        monitor.setState(convFeNode.getState());
        monitor.setUpdateTime(new Date());
        return monitor;
    }
}
