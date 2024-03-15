package com.lrhealth.data.converge.service;

import com.lrhealth.data.model.original.model.OriginalModel;
import com.lrhealth.data.model.original.model.OriginalModelColumn;

import java.util.List;
import java.util.Map;

/**
 * @author jinmengyu
 * @date 2023-08-21
 */
public interface OdsModelService {

    String getTableDataType(String odsTableName, String sysCode);

    Map<String, String> getOdsColumnTypeMap(String odsModelName, String sysCode);

    List<OriginalModelColumn> getColumnList(String odsModelName, String sysCode);

    List<OriginalModel> getModelList(List<Long> modelIdList);
}
