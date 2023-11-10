package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;

/**
 * @author jinmengyu
 * @date 2023-11-01
 */
public interface AsyncExecService {

    void taskExec(ConvTunnel tunnel, Integer taskId);

}
