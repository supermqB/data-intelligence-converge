package com.lrhealth.data.converge.scheduled.service;

import com.lrhealth.data.converge.scheduled.config.exception.*;
import com.lrhealth.data.converge.scheduled.model.TaskFileConfig;
import com.lrhealth.data.converge.scheduled.model.dto.PreFileStatusDto;

public interface TaskFileService {

    public boolean splitFile(TaskFileConfig taskFileConfig);

    public boolean recoverSplit(FileSplitException e);

    public PreFileStatusDto getFileStatus(TaskFileConfig taskFileConfig);

    public PreFileStatusDto recoverStatus(FileStatusException e);

    public boolean downloadFile(PreFileStatusDto preFileStatusDto, TaskFileConfig taskFileConfig);

    public boolean recoverDownload(FileDownloadException e);

    public boolean mergeFile(TaskFileConfig taskFileConfig);

    public boolean recoverMerge(FileMergeException e);

    public boolean deleteFile(TaskFileConfig taskFileConfig);

    public boolean recoverDelete(FileDeleteException e);
}
