package com.lrhealth.data.converge.dao.service;

import com.lrhealth.data.converge.model.dto.MonitorMsg;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvFeNode;

/**
 * @author admin
 */
public interface ConvMonitorService {
    /**
     * 处理汇聚前置机监测信息
     * @param monitorMsg 监测消息
     */
    void handleMonitorMsg(MonitorMsg monitorMsg);

    /**
     * 处理汇聚监视器信息
     * @param convFeNode   前置机
     * @param message      监测消息
     */
    void processConvMonitor(ConvFeNode convFeNode, MonitorMsg message);
}
