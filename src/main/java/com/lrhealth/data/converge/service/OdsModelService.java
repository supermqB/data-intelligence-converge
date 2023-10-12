package com.lrhealth.data.converge.service;

import java.util.Map;

/**
 * @author jinmengyu
 * @date 2023-08-21
 */
public interface OdsModelService {

    String getTableDataType(String odsTableName, String sysCode);

    Map<String, String> getOdsColumnTypeMap(String odsModelName, String sysType);
}
