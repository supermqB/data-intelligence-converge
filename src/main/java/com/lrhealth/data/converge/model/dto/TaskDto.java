package com.lrhealth.data.converge.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 任务信息
 * </p>
 *
 * @author lr
 * @since 2023-07-19
 */
@Data
public class TaskDto {
    /**
     * 项目ID
     */
    private String projectId;
    /**
     * 任务实例ID（任务队列系统）
     */
    private String taskInstanceId;
    /**
     * 任务实例名称（任务队列系统）
     */
    private String taskInstanceName;
    /**
     * ODS数据存储表名
     */
    private String odsTableName;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 数据抽取批次号
     */
    private String batchNo;
    /**
     * xdsID
     */
    private Long xdsId;
    /**
     * 数据记录条数
     */
    private String countNumber;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 任务状态
     */
    private boolean taskStatus;
    /**
     * TODO: 是否使用convergeConfig的converge_method来控制任务的类型？但是dataX生成csv过程的模式？
     * 任务模式: 0db、1file
     */
    private int taskModel;
    /**
     * dataX生成的csv文件名
     */
    private String oriFileName;
}
