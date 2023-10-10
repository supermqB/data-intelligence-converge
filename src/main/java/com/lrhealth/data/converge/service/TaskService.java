package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.FileExecInfoDTO;
import com.lrhealth.data.converge.model.FlinkTaskDto;
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
    FileExecInfoDTO createTask(TaskDto taskDto);

    /**
     * 更新任务状态
     * @param taskDto 任务信息
     * @return XDS信息
     */
    Xds updateTask(TaskDto taskDto);

    /**
     * 前置机文件模式
     * 1.调用前置机接口获得指定目录下的文件列表
     * 2.生成xds后进行解析落库
     */
    void fepConverge(String projectId);

    /**
     *  flink汇聚模式，包含库表采集和文件采集
     */
    Xds flinkConverge(FlinkTaskDto flinkTaskDto);

    /**
     * 数据汇聚流程
     * 文件处在汇聚服务器
     * 通过di_conv_task_result_view新建xds,数据落库后再进行更新
     * 异步执行
     * @param taskResultViewId
     */
    void fileParseAndSave(Integer taskResultViewId);
}
