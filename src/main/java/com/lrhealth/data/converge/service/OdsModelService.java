package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.StdOriginalModel;
import com.lrhealth.data.converge.dao.entity.StdOriginalModelColumn;

import java.util.List;

/**
 * @author jinmengyu
 * @date 2023-08-21
 */
public interface OdsModelService {

    String getTableDataType(String odsTableName, String sysCode);

    List<StdOriginalModelColumn> getColumnList(String odsModelName, String sysCode);

    StdOriginalModel getModel(Long modelId);
}
