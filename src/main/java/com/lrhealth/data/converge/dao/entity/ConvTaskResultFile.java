package com.lrhealth.data.converge.dao.entity;

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
 * @since 2023-11-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("conv_task_result_file")
public class ConvTaskResultFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer taskId;

    private String tableName;

    private String storedPath;

    private Long dataItemCount;

    private LocalDateTime storedTime;

    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Integer delFlag;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Long dataSize;

    private Long transferTime;

    private String feStoredPath;

    private String feStoredFilename;

    private Integer status;

    private Integer fileType;


}
