package com.lrhealth.data.converge.dao.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        if (MonitorMsg.MsgTypeEnum.WRITER_DB_CHECK.getMsgTypeCode().equals(message.getMsgType())){
            processDbMonitor(message,MonitorMsg.MsgTypeEnum.WRITER_DB_CHECK);
            return;
        }
        if (MonitorMsg.MsgTypeEnum.READER_DB_CHECK.getMsgTypeCode().equals(message.getMsgType())){
            processDbMonitor(message,MonitorMsg.MsgTypeEnum.READER_DB_CHECK);
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
            convMonitor.setState(1);
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

    /**
     * 处理汇聚监视器信息
     *
     * @param monitorDTO 前置机
     * @param message    监测消息
     */
    @Override
    public synchronized void processConvMonitor(MonitorDTO monitorDTO, MonitorMsg message) {
       /* monitorDTO.setStatus(message.getStatus());
        ConvMonitor monitor = buildConvMonitor(monitorDTO, message);
        if (monitor.getId() == null) {
            convMonitorMapper.insert(monitor);
            monitorDTO.setId(monitor.getId());
        } else {
            convMonitorMapper.updateById(monitor);
        }
        //首次插入 或 有异常 或 状态变更时 操作库

        Boolean currentStatus = message.getStatus();
        Boolean cacheStatus = monitorDTO.getStatus();
        if (monitor.getId() == null || !currentStatus || !currentStatus.equals(cacheStatus)) {
        if (monitor.getId() == null) {
            convMonitorMapper.insert(monitor);
            monitorDTO.setId(monitor.getId());
        } else {
            convMonitorMapper.updateById(monitor);
        }
        }*/
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
                .eq(ConvMonitor::getOrgCode, message.getOrgCode())
                .eq(ConvMonitor::getSysCode, convFeNode.getSysCode());
        List<ConvMonitor> convMonitorList = convMonitorMapper.selectList(queryWrapper);
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
        monitor.setUpdateTime(new Date());
        monitor.setId(monitorDTO.getId());
        monitor.setConvFeNodeId(monitorDTO.getConvFeNodeId());
        monitor.setSysCode(monitorDTO.getSysCode());
        monitor.setOrgCode(monitorDTO.getOrgCode());
        monitor.setMonitorType(monitorDTO.getMonitorType());
        monitor.setState(message.getStatus() ? 0 : 1);
        monitor.setExceptionDes(message.getMsg());
        if (!message.getStatus()) {
            monitor.setExceptionTime(Objects.isNull(message.getSendTime()) ? new Date() : message.getSendTime());
        }
        return monitor;
    }
}
