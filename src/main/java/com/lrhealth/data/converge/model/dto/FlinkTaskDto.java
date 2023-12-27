package com.lrhealth.data.converge.model.dto;

import lombok.Data;

/**
 * flinkCDC采集数据新建xds
 * 两种方式:1.db, flink直接落库，通知converge生成xds
 *         2.file, flink将文件落到服务器目录，通知converge,解析进行搬运落库
 * @author jinmengyu
 * @date 2023-07-28
 */
@Data
public class FlinkTaskDto {

    /**
     * xdsId flink传入生成xds
     */
    private Long xdsId;
    /**
     * 汇聚时间
     */
    private Long convergeTime;
    /**
     * flink生成文件路径，存储在flink服务所在服务器
     */
    private String filePath;
    /**
     * flink 抽取数据落库表名
     */
    private String tableName;
    /**
     * flink任务名称，唯一
     */
    private String jobName;
    /**
     * 汇聚模式：0:DB; 1:FILE
     */
    private Integer type;
    /**
     * 系统编码
     */
    private String sourceId;
}
