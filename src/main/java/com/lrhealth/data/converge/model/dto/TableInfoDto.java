package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author jinmengyu
 * @date 2023-09-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableInfoDto {

    /**
     * 表名称
     */
    private String tableName;

    /**
     * sql语句
     */
    private String sqlQuery;

    /**
     * 增量字段集合
     */
    private List<String> seqFields;

    /**
     * 增量字段-增量采集最新时间
     */
    private Map<String, String> incrTimeMap;
}
