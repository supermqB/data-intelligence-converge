package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.model.dto.FepScheduledDto;

/**
 * @author jinmengyu
 * @date 2025-04-21
 */
public interface ConvergeTaskService {

    FepScheduledDto scheduleConfig(Long tunnelId, Integer taskId);
}
