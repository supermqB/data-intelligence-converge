package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.model.bo.ColumnDbBo;
import com.lrhealth.data.converge.model.dto.DataSourceDto;

import java.util.List;

/**
 * @author jinmengyu
 * @date 2023-10-24
 */
public interface DbSqlService {

    /**
     * 生成建表语句
     * @param header key字段英文名，value数据类型
     * @param odsTableName ods表名
     * @param dataSourceDto 数据源连接信息
     */
    void createTable(List<ColumnDbBo> header, String odsTableName, DataSourceDto dataSourceDto);

    /**
     * 检查ods表是否创建
     * @param odsTableName ods表名
     * @param dataSourceDto 数据源连接信息
     */
    boolean checkOdsTableExist(String odsTableName, DataSourceDto dataSourceDto);

    String getAvgRowLength(String odsTableName, DataSourceDto dataSourceDto);
}
