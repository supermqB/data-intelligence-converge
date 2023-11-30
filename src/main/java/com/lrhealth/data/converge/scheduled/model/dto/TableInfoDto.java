package com.lrhealth.data.converge.scheduled.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
     * 增量字段
     */
    private List<String> seqFields;
}
