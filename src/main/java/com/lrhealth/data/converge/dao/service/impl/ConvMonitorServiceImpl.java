package com.lrhealth.data.converge.dao.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.util.StringUtils;
import com.lrhealth.data.converge.dao.entity.ConvFeNode;
import com.lrhealth.data.converge.dao.entity.ConvMonitor;
import com.lrhealth.data.converge.dao.mapper.ConvFeNodeMapper;
import com.lrhealth.data.converge.dao.mapper.ConvMonitorMapper;
import com.lrhealth.data.converge.dao.service.ConvMonitorService;
import com.lrhealth.data.converge.model.dto.MonitorDTO;
import com.lrhealth.data.converge.model.dto.MonitorMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author admin
 */
@Slf4j
@Service
public class ConvMonitorServiceImpl implements ConvMonitorService {
    @Resource
    private ConvFeNodeMapper convFeNodeMapper;
    @Resource
    private ConvMonitorMapper convMonitorMapper;

    @Async
    @Override
    public void handleMonitorMsg(MonitorMsg message) {
        if (MonitorMsg.MsgTypeEnum.FEP_STA.getMsgTypeCode().equals(message.getMsgType())){
            //根据IP端口机构编码查询所有前置机
            List<ConvFeNode> convFeNodes = queryFeNodeFromDatabase(message);
            processFepConvMonitor(convFeNodes, message);
            return;
        }
        if (MonitorMsg.MsgTypeEnum.TARGET_DB_CHECK.getMsgTypeCode().equals(message.getMsgType())){
            processDbMonitor(message,MonitorMsg.MsgTypeEnum.TARGET_DB_CHECK);
            return;
        }
        if (MonitorMsg.MsgTypeEnum.MIRROR_DB_CHECK.getMsgTypeCode().equals(message.getMsgType())){
            processDbMonitor(message,MonitorMsg.MsgTypeEnum.MIRROR_DB_CHECK);
            return;
        }
        if (MonitorMsg.MsgTypeEnum.MIRROR_DB_INCR_CHECK.getMsgTypeCode().equals(message.getMsgType())){
            List<String> tableNames = message.getTableNames();
            for (String tableName : tableNames) {
                processMirrorDbIncrMonitor(message,tableName);
            }
        }
    }


    /**
     * 处理目标库监测消息
     */
    private synchronized void processDbMonitor(MonitorMsg message, MonitorMsg.MsgTypeEnum msgTypeEnum) {
        if (StringUtils.isEmpty(message.getOrgCode()) || message.getDsId() == null){
            return;
        }
        LambdaQueryWrapper<ConvMonitor> queryWrapper = new LambdaQueryWrapper<ConvMonitor>()
                .eq(ConvMonitor::getMonitorType, message.getMsgType())
                .eq(ConvMonitor::getOrgCode, message.getOrgCode())
                .eq(ConvMonitor::getDsId, message.getDsId());
        List<ConvMonitor> monitors = convMonitorMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(monitors)){
            ConvMonitor convMonitor = monitors.get(0);
            convMonitor.setState(message.getStatus() ? 0 : 1);
            convMonitor.setExceptionDes(message.getMsg());
            convMonitor.setUpdateTime(new Date());
            convMonitorMapper.updateById(convMonitor);
        }else {
            ConvMonitor convMonitor = new ConvMonitor();
            convMonitor.setOrgCode(message.getOrgCode());
            convMonitor.setDsId(message.getDsId());
            convMonitor.setState(message.getStatus() ? 0 : 1);
            convMonitor.setExceptionDes(message.getMsg());
            convMonitor.setMonitorType(msgTypeEnum.getMsgTypeCode());
            convMonitor.setUpdateTime(new Date());
            convMonitorMapper.insert(convMonitor);
        }
    }

    /**
     * 处理镜像库增量数据
     */
    private synchronized void processMirrorDbIncrMonitor(MonitorMsg message,String tableName) {
        LambdaQueryWrapper<ConvMonitor> queryWrapper = new LambdaQueryWrapper<ConvMonitor>()
                .eq(ConvMonitor::getTableName, tableName)
                .eq(ConvMonitor::getMonitorType, message.getMsgType())
                .eq(ConvMonitor::getOrgCode, message.getOrgCode())
                .eq(ConvMonitor::getDsId, message.getDsId());
        List<ConvMonitor> monitors = convMonitorMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(monitors)){
            ConvMonitor convMonitor = monitors.get(0);
            convMonitor.setState(0);
            convMonitor.setUpdateTime(new Date());
            convMonitorMapper.updateById(convMonitor);
        }else {
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
        List<ConvMonitor> convMonitorList = null;
        //第一种处理前置机类型
        if (MonitorMsg.MsgTypeEnum.FEP_STA.getMsgTypeCode().equals(message.getMsgType())) {
            convMonitorList = queryFeNodeConvMonitor(convFeNodes, message);
        }
        //目标库监控
        /*if (MonitorMsg.MsgTypeEnum.READER_DB_CHECK.getMsgTypeCode().equals(message.getMsgType())
                || MonitorMsg.MsgTypeEnum.WRITER_DB_CHECK.getMsgTypeCode().equals(message.getMsgType())) {
            convMonitorList = queryDBConnectConvMonitor(convFeNodes, message);
        }*/
        Map<Long, ConvMonitor> convMonitorMap = null;
        if (CollectionUtils.isNotEmpty(convMonitorList)) {
            convMonitorMap = convMonitorList.stream().collect(Collectors.toMap(ConvMonitor::getConvFeNodeId, e -> e, (m1, m2) -> m1));
        }
        List<ConvMonitor> monitorList = new ArrayList<>();
        for (ConvFeNode feNode : convFeNodes) {
            ConvMonitor convMonitor = buildConvMonitor(feNode, convMonitorMap, message);
            monitorList.add(convMonitor);
        }
        //更新数据库
        for (ConvMonitor monitor : monitorList) {
            if (monitor.getId() == null) {
                convMonitorMapper.insert(monitor);
            } else {
                convMonitorMapper.updateById(monitor);
            }
        }
    }

    private List<ConvMonitor> queryDBConnectConvMonitor(List<ConvFeNode> convFeNodes, MonitorMsg message) {
        if (CollectionUtils.isEmpty(convFeNodes)) {
            return null;
        }
        List<Long> convFeIds = convFeNodes.stream().map(ConvFeNode::getId).collect(Collectors.toList());
        //查询数据库连接消息
        LambdaQueryWrapper<ConvMonitor> queryWrapper = new LambdaQueryWrapper<ConvMonitor>()
                .in(ConvMonitor::getConvFeNodeId, convFeIds)
                .eq(ConvMonitor::getMonitorType, message.getMsgType())
                .eq(ConvMonitor::getOrgCode, message.getOrgCode())
                .eq(ConvMonitor::getDsId, message.getDsId());
        return convMonitorMapper.selectList(queryWrapper);
    }

    private List<ConvMonitor> queryFeNodeConvMonitor(List<ConvFeNode> convFeNodes, MonitorMsg message) {
        if (CollectionUtils.isEmpty(convFeNodes)) {
            return null;
        }
        //机构下所有前置机
        List<Long> convFeIds = convFeNodes.stream().map(ConvFeNode::getId).collect(Collectors.toList());
        //查询监控消息
        LambdaQueryWrapper<ConvMonitor> queryWrapper = new LambdaQueryWrapper<ConvMonitor>()
                .in(ConvMonitor::getConvFeNodeId, convFeIds)
                .eq(ConvMonitor::getMonitorType, message.getMsgType())
                .eq(ConvMonitor::getOrgCode, message.getOrgCode());
        return convMonitorMapper.selectList(queryWrapper);
    }

    /**
     * 数据库查询前置机信息
     */
    private List<ConvFeNode> queryFeNodeFromDatabase(MonitorMsg message) {
        LambdaQueryWrapper<ConvFeNode> queryWrapper = new LambdaQueryWrapper<ConvFeNode>()
                .eq(ConvFeNode::getIp, message.getSourceIp())
                .eq(ConvFeNode::getPort, Integer.parseInt(message.getSourcePort()));
        //管道监控
        /*if (message.getTunnelId() != null) {
            ConvTunnel convTunnel = convTunnelMapper.selectOne(new LambdaQueryWrapper<ConvTunnel>()
                    .eq(ConvTunnel::getId, message.getTunnelId()));
            if (convTunnel == null || convTunnel.getFrontendId() < 0) {
                log.info("<----前置机不存在,采用库到库直连方式---->");
                return null;
            }
            queryWrapper.eq(ConvFeNode::getId, convTunnel.getFrontendId());
        }*/
        queryWrapper.eq(ConvFeNode::getOrgCode, message.getOrgCode());
        return convFeNodeMapper.selectList(queryWrapper);
    }

    /**
     * 根据心跳构建监控信息
     */
    private ConvMonitor buildConvMonitor(ConvFeNode feNode, Map<Long, ConvMonitor> convMonitorMap, MonitorMsg message) {
        ConvMonitor newMonitor = new ConvMonitor();
        newMonitor.setConvFeNodeId(feNode.getId());
        newMonitor.setSysCode(feNode.getSysCode());
        newMonitor.setOrgCode(feNode.getOrgCode());
        if (convMonitorMap != null && convMonitorMap.get(feNode.getId()) != null) {
            ConvMonitor originalMonitor = convMonitorMap.get(feNode.getId());
            BeanUtil.copyProperties(originalMonitor, newMonitor);
        }
        newMonitor.setDsId(message.getDsId());
        newMonitor.setUpdateTime(new Date());
        newMonitor.setMonitorType(message.getMsgType());
        newMonitor.setState(message.getStatus() ? 0 : 1);
        newMonitor.setExceptionDes(message.getMsg());
        newMonitor.setExceptionTime(message.getStatus() ? null : message.getSendTime());
        return newMonitor;
    }

    @Override
    public List<ConvFeNode> getAliveFepInfoByMonitor() {
        List<ConvFeNode> feNodes = convMonitorMapper.selectFepByMonitor();
        //TODO: 超过十分钟前置机默认下线即非存活状态
        if (CollectionUtils.isNotEmpty(feNodes)){
            Date nowTime = new Date();
            return feNodes.stream()
                    .filter(entity -> Duration.between(entity.getUpdateTime().toInstant(), nowTime.toInstant()).toMinutes() <= 10)
                    .collect(Collectors.toList());
        }
        return null;
    }
}
