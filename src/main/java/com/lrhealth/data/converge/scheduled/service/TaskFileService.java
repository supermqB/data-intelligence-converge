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

    /**
     * 数据汇聚流程
     * 文件处在汇聚服务器
     * 通过di_conv_task_result_view新建xds,数据落库后再进行更新
     * 异步执行
     * @param taskResultViewId
     */
    void fileParseAndSave(Integer taskResultViewId);
}
