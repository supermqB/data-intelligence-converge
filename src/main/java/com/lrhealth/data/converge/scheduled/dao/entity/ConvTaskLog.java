package com.lrhealth.data.converge.scheduled.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangguowen-generator
 * @since 2023-09-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("di_conv_task_log")
public class ConvTaskLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务日志主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务主键id
     */
    private Long taskId;

    /**
     * 日志详情
     */
    private String logDetail;

    /**
     * 日志时间
     */
    private LocalDateTime timestamp;

    /**
     * dataX或者其它引擎的任务id
     */
    private Long jobId;


}
