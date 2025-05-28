package com.lrhealth.data.converge.model.dto;

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
     * writer写入字段列表
     */
    private String writerColumns;
    /**
     * hive表存储路径
     */
    private String hdfsPath;
    /**
     * hive文件存储格式
     */
    private String hiveFileType;

    private String hivePartitionColumn;
    /**
     * 增量字段
     */
    private String seqField;

    private List<IncrColumnDTO> incrConfigList;
}
