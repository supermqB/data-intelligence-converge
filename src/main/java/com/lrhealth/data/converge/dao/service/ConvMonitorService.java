package com.lrhealth.data.converge.dao.service;

import com.lrhealth.data.converge.dao.entity.ConvFeNode;
import com.lrhealth.data.converge.model.dto.MonitorMsg;

import java.util.List;

/**
 * @author admin
 */
public interface ConvMonitorService {
    /**
     * 处理汇聚前置机监测信息
     *
     * @param monitorMsg 监测消息
     */
    void handleMonitorMsg(MonitorMsg monitorMsg);


    /**
     * 根据监测信息获取前置机
     * @return list
     */
    List<ConvFeNode> getAliveFepInfoByMonitor();

}
