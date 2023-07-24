package com.lrhealth.data.converge.model;

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
     * 医院编码（发生地编码）
     */
    private String hpsCode;
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
     * 数据记录条数
     */
    private String countNumber;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 任务状态
     */
    private boolean taskStatus;
    /**
     * xdsID
     */
    private Long xdsId;
}
