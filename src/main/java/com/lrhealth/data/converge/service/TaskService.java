package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.TaskDto;

/**
 * <p>
 * 任务接口
 * </p>
 *
 * @author lr
 * @since 2023-07-19
 */
public interface TaskService {

    /**
     * 创建任务
     *
     * @param taskDto 任务信息
     * @return XDS信息
     */
    Xds createTask(TaskDto taskDto);
}
