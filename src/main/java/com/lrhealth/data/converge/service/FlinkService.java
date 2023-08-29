package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.DataXExecDTO;

/**
 * @author jinmengyu
 * @date 2023-08-08
 */
public interface FlinkService {

    Xds database(Xds xds);

    Xds file(DataXExecDTO dataXExecDTO, Long xdsId, String oriFilePath);
}
