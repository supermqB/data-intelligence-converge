package com.lrhealth.data.converge.service;


import com.lrhealth.data.converge.model.TaskFileConfig;
import com.lrhealth.data.converge.model.dto.PreFileStatusDto;

public interface TaskFileService {

    public boolean splitFile(TaskFileConfig taskFileConfig);

    public PreFileStatusDto getFileStatus(TaskFileConfig taskFileConfig);


    public boolean downloadFile(PreFileStatusDto preFileStatusDto, TaskFileConfig taskFileConfig);

    public boolean mergeFile(TaskFileConfig taskFileConfig);
}
