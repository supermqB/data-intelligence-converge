package com.lrhealth.data.converge.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.util.StringUtils;
import com.lrhealth.data.converge.dao.entity.ConvFeNode;
import com.lrhealth.data.converge.dao.entity.ConvMonitor;
import com.lrhealth.data.converge.dao.entity.ConvOdsDatasourceConfig;
import com.lrhealth.data.converge.dao.mapper.ConvFeNodeMapper;
import com.lrhealth.data.converge.dao.mapper.ConvMonitorMapper;
import com.lrhealth.data.converge.dao.service.ConvFeNodeService;
import com.lrhealth.data.converge.dao.service.ConvMonitorService;
import com.lrhealth.data.converge.dao.service.ConvOdsDatasourceConfigService;
import com.lrhealth.data.converge.model.dto.MonitorMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author admin
 */
@Slf4j
@Service
public class ConvMonitorServiceImpl implements ConvMonitorService {
    @Resource
    private ConvFeNodeMapper convFeNodeMapper;
    @Resource
    private ConvOdsDatasourceConfigService convOdsDatasourceConfigService;
    @Resource
    private ConvFeNodeService convFeNodeService;
    @Resource
    private ConvMonitorMapper convMonitorMapper;

    @Async
    @Override
    public void handleMonitorMsg(MonitorMsg message) {
        if (MonitorMsg.MsgTypeEnum.FEP_STA.getMsgTypeCode().equals(message.getMsgType())) {
            List<ConvFeNode> convFeNodes = getFeNodeByMsg(message);
            processFepConvMonitor(convFeNodes, message);
            return;
        }
        if (MonitorMsg.MsgTypeEnum.MIRROR_DB_CHECK.getMsgTypeCode().equals(message.getMsgType())
                || MonitorMsg.MsgTypeEnum.TARGET_DB_CHECK.getMsgTypeCode().equals(message.getMsgType())) {
            processDbMonitor(message);
            return;
        }
        if (MonitorMsg.MsgTypeEnum.MIRROR_DB_INCR_CHECK.getMsgTypeCode().equals(message.getMsgType())) {
            List<String> tableNames = message.getTableNames();
            for (String tableName : tableNames) {
                processMirrorDbIncrMonitor(message, tableName);
            }
        }
    }


    /**
     * 处理目标库监测消息
     */
    private synchronized void processDbMonitor(MonitorMsg message) {
        if (StringUtils.isEmpty(message.getOrgCode()) || message.getDsId() == null) {
            return;
        }
        List<ConvOdsDatasourceConfig> dsConfigs = convOdsDatasourceConfigService.list(new LambdaQueryWrapper<ConvOdsDatasourceConfig>()
                .eq(ConvOdsDatasourceConfig::getOrgCode, message.getOrgCode())
                .eq(ConvOdsDatasourceConfig::getId, message.getDsId()).eq(ConvOdsDatasourceConfig::getDelFlag, 0));
        if (CollectionUtils.isEmpty(dsConfigs)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        dsConfigs.forEach(dsConf -> {
            dsConf.setUpdateTime(now);
            dsConf.setHeartBeatTime(message.getStatus() ? now : null);
        });
        convOdsDatasourceConfigService.updateBatchById(dsConfigs);
    }

    /**
     * 处理镜像库增量数据
     */
    private synchronized void processMirrorDbIncrMonitor(MonitorMsg message, String tableName) {
        LambdaQueryWrapper<ConvMonitor> queryWrapper = new LambdaQueryWrapper<ConvMonitor>()
                .eq(ConvMonitor::getTableName, tableName)
                .eq(ConvMonitor::getMonitorType, message.getMsgType())
                .eq(ConvMonitor::getOrgCode, message.getOrgCode())
                .eq(ConvMonitor::getDsId, message.getDsId());
        List<ConvMonitor> monitors = convMonitorMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(monitors)) {
            ConvMonitor convMonitor = monitors.get(0);
            convMonitor.setState(0);
            convMonitor.setUpdateTime(new Date());
            convMonitorMapper.updateById(convMonitor);
        } else {
            ConvMonitor convMonitor = new ConvMonitor();
            convMonitor.setTableName(tableName);
            convMonitor.setState(0);
            convMonitor.setOrgCode(message.getOrgCode());
            convMonitor.setDsId(message.getDsId());
            convMonitor.setMonitorType(MonitorMsg.MsgTypeEnum.MIRROR_DB_INCR_CHECK.getMsgTypeCode());
            convMonitor.setUpdateTime(new Date());
            convMonitorMapper.insert(convMonitor);
        }
    }

    /**
     * 处理前置机状态监控
     *
     * @param convFeNodes 前置机
     * @param message     监控消息
     */
    public synchronized void processFepConvMonitor(List<ConvFeNode> convFeNodes, MonitorMsg message) {
        if (CollectionUtils.isEmpty(convFeNodes)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        convFeNodes.forEach(convFeNode -> {
            convFeNode.setState(1);
            convFeNode.setUpdateTime(now);
            convFeNode.setHeartBeatTime(now);
        });
        convFeNodeService.updateBatchById(convFeNodes);
    }

    /**
     * 数据库查询前置机信息
     */
    private List<ConvFeNode> getFeNodeByMsg(MonitorMsg message) {
        LambdaQueryWrapper<ConvFeNode> queryWrapper = new LambdaQueryWrapper<ConvFeNode>()
                .eq(StringUtils.isNotEmpty(message.getSourceIp()), ConvFeNode::getIp, message.getSourceIp())
                .eq(StringUtils.isNotEmpty(message.getSourcePort()), ConvFeNode::getPort, Integer.parseInt(message.getSourcePort()))
                .eq(StringUtils.isNotEmpty(message.getOrgCode()), ConvFeNode::getOrgCode, message.getOrgCode())
                .eq(ConvFeNode::getDelFlag, 0);
        queryWrapper.eq(ConvFeNode::getOrgCode, message.getOrgCode());
        return convFeNodeMapper.selectList(queryWrapper);
    }

}
