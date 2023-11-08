package com.lrhealth.data.converge.scheduled.service;


import com.lrhealth.data.converge.scheduled.dao.entity.ConvTask;
import com.lrhealth.data.converge.scheduled.model.FileTask;
import com.lrhealth.data.converge.scheduled.model.TaskFileConfig;

import java.util.concurrent.ConcurrentLinkedDeque;

public interface ConvergeService {

    void updateFeNodeStatus(Long feNodeId, ConcurrentLinkedDeque<FileTask> taskDeque);

    void updateDownLoadFileTask(ConcurrentLinkedDeque<FileTask> taskDeque);

    void resetStatus(ConvTask convTask, TaskFileConfig taskFileConfig);

    TaskFileConfig getTaskConfig(FileTask fileTask);

    boolean updateFileStatus(TaskFileConfig taskFileConfig, long l);
}
