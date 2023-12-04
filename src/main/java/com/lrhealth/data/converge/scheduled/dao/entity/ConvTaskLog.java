package com.lrhealth.data.converge.scheduled.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

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
@TableName("conv_task_log")
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    private Integer taskId;

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
    private Integer fedLogId;


}
