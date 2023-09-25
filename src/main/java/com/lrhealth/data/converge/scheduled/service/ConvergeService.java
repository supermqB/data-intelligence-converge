package com.lrhealth.data.converge.scheduled.service;


import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultView;
import com.lrhealth.data.converge.scheduled.model.FileTask;
import com.lrhealth.data.converge.scheduled.model.dto.PreFileStatusDto;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedDeque;

public interface ConvergeService {

    Boolean ping(String ip, String port);

    void updateFeNodeStatus(Long feNodeId, ConcurrentLinkedDeque<FileTask> taskDeque);

    void updateDownLoadFileTask(ConcurrentLinkedDeque<FileTask> taskDeque);

    String prepareFiles(String url, FileTask fileTask);

    PreFileStatusDto getPreFilesStatus(String url, FileTask fileTask) throws Exception;

    void downLoadFile(String url, File file, FileTask fileTask, PreFileStatusDto preFileStatusDto);

    void deleteFiles(String url, FileTask fileTask);

    void updateFileStatus(ConvTaskResultView taskResultView);
}
