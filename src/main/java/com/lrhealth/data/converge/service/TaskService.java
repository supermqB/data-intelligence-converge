package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.FepFileInfoVo;
import com.lrhealth.data.converge.model.FlinkTaskDto;
import com.lrhealth.data.converge.model.TaskDto;

import java.util.List;

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

    /**
     * 更新任务状态
     * @param taskDto 任务信息
     * @return XDS信息
     */
    Xds updateTask(TaskDto taskDto);

    /**
     * 文件汇聚流程
     */
    List<FepFileInfoVo> fileConverge(String projectId);

    /**
     * 文件存储在本地服务器，汇聚项目直接进行搬运->解析->落库
     */
    void localFileParse(String projectId);

    Xds flinkCreateXds(FlinkTaskDto flinkTaskDto);
}
