package com.lrhealth.data.converge.service;


import com.lrhealth.data.converge.dao.entity.ConvTask;
import com.lrhealth.data.converge.model.FileTask;
import com.lrhealth.data.converge.model.TaskFileConfig;

public interface ConvergeService {

    void resetStatus(ConvTask convTask, TaskFileConfig taskFileConfig);

    TaskFileConfig getTaskConfig(FileTask fileTask);

    boolean updateFileStatus(TaskFileConfig taskFileConfig, long l);

    void sendDsKafka(ConvTask convTask, ConvTask oldTask, Long tunnelId);
}
