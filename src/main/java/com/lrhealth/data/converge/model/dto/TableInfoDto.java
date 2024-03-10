package com.lrhealth.data.converge.model.dto;

import com.lrhealth.data.converge.common.enums.SeqFieldTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * writer写入字段列表
     */
    private String writerColumns;

    /**
     * hive表存储路径
     */
    private String hdfsPath;

    /**
     * 增量字段
     */
    private String seqField;

    /**
     * 增量字段类型
     * @see SeqFieldTypeEnum
     */
    private String seqFieldType;

    /**
     * 增量字段-增量采集最新时间
     */
    private Map<String, String> incrTimeMap;
}
