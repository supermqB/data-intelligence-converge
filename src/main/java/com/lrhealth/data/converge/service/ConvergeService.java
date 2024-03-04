package com.lrhealth.data.converge.service;


import com.lrhealth.data.converge.dao.entity.ConvTask;
import com.lrhealth.data.converge.model.FileTask;
import com.lrhealth.data.converge.model.TaskFileConfig;
import com.lrhealth.data.converge.model.dto.FrontendStatusDto;

import java.util.concurrent.ConcurrentLinkedDeque;

public interface ConvergeService {

    void updateFeNodeStatus(Long feNodeId, ConcurrentLinkedDeque<FileTask> taskDeque);

    void updateDownLoadFileTask(ConcurrentLinkedDeque<FileTask> taskDeque);

    void resetStatus(ConvTask convTask, TaskFileConfig taskFileConfig);

    TaskFileConfig getTaskConfig(FileTask fileTask);

    boolean updateFileStatus(TaskFileConfig taskFileConfig, long l);

    void updateFepStatus(FrontendStatusDto frontendStatusDto, ConcurrentLinkedDeque<FileTask> taskDeque);

    void sendDsKafka(ConvTask convTask, ConvTask oldTask, Long tunnelId);
}
