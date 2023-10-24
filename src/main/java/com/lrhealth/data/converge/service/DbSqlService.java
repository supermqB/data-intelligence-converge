package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.model.bo.ColumnDbBo;

import java.util.List;

/**
 * @author jinmengyu
 * @date 2023-10-24
 */
public interface DbSqlService {

    /**
     * 生成建表语句
     * @param header key字段英文名，value数据类型
     * @param odsTableName
     * @return
     */
    void createTable(List<ColumnDbBo> header, String odsTableName);

    /**
     * 检查ods表是否创建
     * @param odsTableName
     * @return
     */
    boolean checkOdsTableExist(String odsTableName);
}
