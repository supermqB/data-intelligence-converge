package com.lrhealth.data.converge.scheduled.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("di_conv_task_result_view")
public class ConvTaskResultView implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务结果id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 任务主键id
     */
    private Integer taskId;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 汇聚服务，文件路径+名称
     */
    private String storedPath;

    /**
     * 记录条数
     */
    private Integer dataItemCount;

    /**
     * 开始索引
     */
    private Integer startIndex;

    /**
     * 结束索引
     */
    private Integer endIndex;

    /**
     * 入库时间
     */
    private LocalDateTime storedTime;

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

    /**
     * 文件大小
     */
    private Integer dataSize;

    /**
     * 文件传输时间（单位秒）
     */
    private Long transferTime;

    /**
     * 前置机，文件路径+名称
     */
    private String feStoredPath;

    /**
     * 文件状态：prepared/tranfering/downloaded/failed/stored
     */
    private Integer status;

    /**
     * 文件名
     */
    private String feStoredFilename;
}
