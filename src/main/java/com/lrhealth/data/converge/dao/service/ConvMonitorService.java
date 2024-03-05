package com.lrhealth.data.converge.dao.service;

import com.lrhealth.data.converge.model.dto.MonitorMsg;

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


}
