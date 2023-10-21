package com.lrhealth.data.converge.scheduled.service;

/**
 * @author jinmengyu
 * @date 2023-09-20
 */
public interface StatusService {

    void updateTaskCompleted(Long tunnelId, Integer taskId);
}
