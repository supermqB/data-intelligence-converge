package com.lrhealth.data.converge.service;


import com.lrhealth.data.converge.common.enums.ExecStatusEnum;

/**
 * 文件采集
 * @author jinmengyu
 * @date 2023-11-03
 */
public interface FileTaskService {
    void run(Long tunnelId, Integer taskId, ExecStatusEnum execStatus, Integer oldTaskId);

}
