package com.lrhealth.data.converge.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lrhealth.data.converge.common.enums.TaskStatusEnum;
import com.lrhealth.data.converge.dao.entity.ConvTask;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhangguowen-generator
 * @since 2023-09-18
 */
public interface ConvTaskService extends IService<ConvTask> {

    ConvTask createTask(ConvTunnel tunnel, boolean isCdc);

    void updateTaskStatus(Integer taskId, TaskStatusEnum taskStatusEnum);

    ConvTask cdcFindTask(ConvTunnel tunnel);

}
