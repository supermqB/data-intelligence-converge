package com.lrhealth.data.converge.service.impl;

import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.TaskDto;
import com.lrhealth.data.converge.service.TaskService;
import com.lrhealth.data.converge.service.XdsInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 任务接口实现类
 * </p>
 *
 * @author lr
 * @since 2023/7/19
 */
@Service
public class TaskServiceImpl implements TaskService {
    @Resource
    private XdsInfoService xdsInfoService;

    @Override
    public Xds createTask(TaskDto taskDto) {
        return xdsInfoService.createXdsInfo(taskDto);
    }
}
