package com.lrhealth.data.converge.scheduled.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName("di_conv_task_result_cdc")
public class ConvTaskResultCdc implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 结果id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务主键id
     */
    private Long taskId;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 记录条数
     */
    private Integer dataCount;

    /**
     * 新增条数
     */
    private Integer addCount;

    /**
     * 删除条数
     */
    private Integer deleteCount;

    /**
     * 更新条数
     */
    private Integer updateCount;

    /**
     * 删除标识
     */
    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Integer delFlag;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
