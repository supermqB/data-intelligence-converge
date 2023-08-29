package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.FileExecInfoDTO;

/**
 * @author jinmengyu
 * @date 2023-08-08
 */
public interface FlinkService {

    Xds database(Xds xds);

    Xds file(FileExecInfoDTO fileExecInfoDTO, Long xdsId, String oriFilePath);
}
