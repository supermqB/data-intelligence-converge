package com.lrhealth.data.converge.dao.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.cache.Cache;
import com.lrhealth.data.converge.dao.entity.ConvMonitor;
import com.lrhealth.data.converge.dao.mapper.ConvMonitorMapper;
import com.lrhealth.data.converge.dao.service.ConvMonitorService;
import com.lrhealth.data.converge.model.dto.MonitorDTO;
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
    private static final String TUNNEL_CONCAT = "|";
    private static final String FE_CONCAT = "*";
    private static final String FE_NODE_PREFIX = "fe_node:";


    @Async
    @Override
    public void handleMonitorMsg(MonitorMsg message) {
        //查询缓存中前置机信息
        String feNodeCacheKey;
        if (MonitorMsg.MsgTypeEnum.FEP_STA.getMsgTypeCode().equals(message.getMsgType())
                || MonitorMsg.MsgTypeEnum.CDC_STA.getMsgTypeCode().equals(message.getMsgType())) {
            feNodeCacheKey = FE_NODE_PREFIX.concat(message.getSourceIp())
                    .concat(FE_CONCAT).concat(message.getSourcePort())
                    .concat(FE_CONCAT).concat(message.getOrgCode())
                    .concat(FE_CONCAT).concat(message.getMsgType());
        } else {
            feNodeCacheKey = FE_NODE_PREFIX.concat(message.getSourceIp())
                    .concat(TUNNEL_CONCAT).concat(message.getSourcePort())
                    .concat(TUNNEL_CONCAT).concat(String.valueOf(message.getTunnelId()))
                    .concat(TUNNEL_CONCAT).concat(message.getMsgType());
        }
        Object cacheValue = convCache.getObject(feNodeCacheKey);
        MonitorDTO monitor;
        if (cacheValue == null) {
            ConvFeNode convFeNode = queryFeNodeFromDatabase(message);
            monitor = buildMonitorDTO(convFeNode, message);
        } else {
            monitor = JSON.parseObject(JSON.toJSONString(cacheValue), MonitorDTO.class);
        }
        //消息处理
        if (Objects.isNull(monitor)) {
            log.info("未查询到前置机信息 message = {}", message);
            return;
        }
        processConvMonitor(monitor, message);
        convCache.putObject(feNodeCacheKey, monitor);
    }

    /**
     * 数据库查询前置机信息
     */
    private ConvFeNode queryFeNodeFromDatabase(MonitorMsg message) {
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
            return feNodeList.get(0);
        }
        return null;
    }

    /**
     * 处理汇聚监视器信息
     *
     * @param monitorCache 前置机
     * @param message      监测消息
     */
    @Override
    public synchronized void processConvMonitor(MonitorDTO monitorCache, MonitorMsg message) {
        Boolean currentStatus = message.getStatus();
        Boolean cacheStatus = monitorCache.getStatus();
        ConvMonitor monitor = buildConvMonitor(monitorCache, message);
        //首次插入 或 有异常 或 状态变更时 操作库
        if (monitor.getId() == null || !currentStatus || !currentStatus.equals(cacheStatus)) {
            if (monitor.getId() == null) {
                convMonitorMapper.insert(monitor);
            } else {
                convMonitorMapper.updateById(monitor);
            }
        }
    }


    /**
     * 构建缓存DTO
     *
     * @param convFeNode 前置机
     * @param message    消息
     * @return MonitorDTO
     */
    public MonitorDTO buildMonitorDTO(ConvFeNode convFeNode, MonitorMsg message) {
        if (Objects.isNull(convFeNode)) {
            return null;
        }
        MonitorDTO monitor = new MonitorDTO();
        LambdaQueryWrapper<ConvMonitor> queryWrapper = new LambdaQueryWrapper<ConvMonitor>()
                .eq(ConvMonitor::getConvFeNodeId, convFeNode.getId())
                .eq(ConvMonitor::getMonitorType, message.getMsgType())
                .eq(ConvMonitor::getOrgCode, message.getOrgCode());
        final List<ConvMonitor> convMonitorList = convMonitorMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(convMonitorList)) {
            monitor.setId(convMonitorList.get(0).getId());
        }
        monitor.setConvFeNodeId(convFeNode.getId());
        monitor.setOrgCode(convFeNode.getOrgCode());
        monitor.setSysCode(convFeNode.getSysCode());
        monitor.setMonitorType(message.getMsgType());
        monitor.setStatus(message.getStatus());
        monitor.setUpdateTime(message.getSendTime());
        return monitor;
    }

    /**
     * 构建汇聚前置机监测信息
     *
     * @param monitorDTO 监测信息
     * @param message    监测消息
     * @return 监测信息
     */
    private ConvMonitor buildConvMonitor(MonitorDTO monitorDTO, MonitorMsg message) {
        ConvMonitor monitor = new ConvMonitor();
        monitor.setState(monitorDTO.getStatus() ? 0 : 1);
        monitor.setUpdateTime(new Date());
        monitor.setId(monitorDTO.getId());
        monitor.setConvFeNodeId(monitorDTO.getConvFeNodeId());
        monitor.setSysCode(monitorDTO.getSysCode());
        monitor.setOrgCode(monitorDTO.getOrgCode());
        monitor.setMonitorType(monitorDTO.getMonitorType());
        monitor.setExceptionDes(message.getMsg());
        if (!message.getStatus()) {
            Date exceptionTime = message.getSendTime() == null ? monitorDTO.getUpdateTime() : message.getSendTime();
            monitor.setExceptionTime(exceptionTime);
        }
        return monitor;
    }
}
