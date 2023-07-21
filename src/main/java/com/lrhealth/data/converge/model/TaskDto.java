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
     * 机构编码
     */
    private String orgCode;
    /**
     * 机构名称
     */
    private String orgName;
    /**
     * 系统编码
     */
    private String sysCode;
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
     * 汇聚方式：1-客户写文件;2-客户掉接口，3-平台调接口，4-平台读队列，5-平台读视图，6-平台读库表，7-自主模式
     */
    private String convergeMethod;
    /**
     * 数据类别：1-字典数据;2-基础数据，3-报告文书，4-影像文件，5-业务数据
     */
    private String dataType;
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
