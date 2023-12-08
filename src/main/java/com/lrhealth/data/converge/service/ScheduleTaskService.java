package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.model.dto.FepScheduledDto;

import java.util.List;

/**
 * @author jinmengyu
 * @date 2023-12-07
 */
public interface ScheduleTaskService {

    List<FepScheduledDto> getCacheTask(String ip, Integer port);
}
