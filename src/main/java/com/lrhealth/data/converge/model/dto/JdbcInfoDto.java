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
public class JdbcInfoDto {
    /**
     * Reader 数据源id
     */
    private Integer dsId;
    /**
     * 采集模式
     * 0-库到文件 1-库到库
     */
    private Integer collectModel;
    /**
     * 采集类型
     * 1-单次采集 2-增量采集 3-全量采集
     */
    private Integer colType;
    /**
     * 库表采集范围和sql查询语句
     */
    private List<TableInfoDto> tableInfoDtoList;
    /**
     * 1-单表采集 2-关联采集
     */
    private Integer colTableType;
    /**
     * writer 数据源id
     */
    private Integer dsConfigId;
    /**
     * hdfs集群， dsId列表
     */
    private String hdfsCluster;
}
