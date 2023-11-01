package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.model.dto.TableInfoDto;

import java.util.List;

/**
 * @author jinmengyu
 * @date 2023-09-12
 */
public interface DataXExecService {

    String dataXExec(String jsonPath, Long jobExecResultId, String mode, Integer taskId);

    void run(Long tunnelId, Integer taskId, Integer execStatus, Integer oldTaskId) throws InterruptedException;

    void dataXConfig(ConvTunnel tunnel, List<TableInfoDto> tableInfoDtoList);

}
