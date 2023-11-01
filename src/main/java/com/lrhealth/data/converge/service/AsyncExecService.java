package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;

/**
 * @author jinmengyu
 * @date 2023-11-01
 */
public interface AsyncExecService {

    void tunnelExec(ConvTunnel tunnel, Integer taskId, Integer execStatus, Integer oldTaskId);

}
