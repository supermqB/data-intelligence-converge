package com.lrhealth.data.converge.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.dao.entity.ConvMonitor;
import com.lrhealth.data.converge.dao.mapper.ConvMonitorMapper;
import com.lrhealth.data.converge.dao.service.ConvMonitorService;
import com.lrhealth.data.converge.model.dto.MonitorMsg;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvFeNode;
import com.lrhealth.data.converge.scheduled.dao.mapper.DiConvFeNodeMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author admin
 */
@Service
public class ConvMonitorServiceImpl implements ConvMonitorService {
    @Resource
    private DiConvFeNodeMapper diConvFeNodeMapper;
    @Resource
    private ConvMonitorMapper convMonitorMapper;

    @Override
    public void handleMonitorMsg(MonitorMsg monitorMsg) {
        if (Objects.isNull(monitorMsg)) {
            return;
        }
        List<ConvFeNode> feNodeList = diConvFeNodeMapper.selectList(new LambdaQueryWrapper<ConvFeNode>()
                .eq(ConvFeNode::getIp, monitorMsg.getSourceIp())
                .eq(ConvFeNode::getPort, Integer.parseInt(monitorMsg.getSourcePort())));
        if (CollectionUtils.isNotEmpty(feNodeList)) {
            for (ConvFeNode convFeNode : feNodeList) {
                ConvMonitor convMonitor = convMonitorMapper.selectOne(new LambdaQueryWrapper<ConvMonitor>().eq(ConvMonitor::getConvFeNodeId, convFeNode.getId()));
                ConvMonitor buildConvMonitor = buildConvMonitor(convMonitor, monitorMsg, convFeNode);
                if (convMonitor == null) {
                    convMonitorMapper.insert(buildConvMonitor);
                } else {
                    convMonitorMapper.updateById(buildConvMonitor);
                }
            }
        }

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
        monitor.setMonitorType(message.getMsgType());
        monitor.setExceptionDes(message.getMsg());
        monitor.setExceptionTime(message.getSendTime());
        monitor.setState(convFeNode.getState());
        monitor.setMonitorType(message.getMsgType());
        monitor.setUpdateTime(new Date());
        return monitor;
    }
}
