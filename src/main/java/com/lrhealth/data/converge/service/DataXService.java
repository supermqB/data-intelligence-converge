package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.FileExecInfoDTO;
import com.lrhealth.data.converge.model.TaskDto;

/**
 * dataX采集方式
 * 1.db 生成xds, dataX采集, 更新xds
 * 2.file 生成xds, dataX采集成csv文件, 更新xds, 前置机扫描, 解析落库
 * dataX生成的文件放在同服务器下，与前置机处于同一服务器
 * 由于前置机、汇聚、任务调度可能处于3个不同的服务器，因此通过汇聚触发从前置机扫描到落库的完整流程
 * @author jinmengyu
 * @date 2023-08-25
 */
public interface DataXService {

    /**
     * dataX模式创建xds
     * @param taskDto
     * @return
     */
    FileExecInfoDTO createTask(TaskDto taskDto);

    Xds updateTask(TaskDto taskDto);

    void execDataX(String jsonList, String jsonSavePath);
}
