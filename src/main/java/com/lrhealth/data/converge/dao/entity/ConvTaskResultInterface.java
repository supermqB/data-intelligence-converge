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
 * @since 2023-09-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("conv_task_result_interface")
public class ConvTaskResultInterface implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务结果id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;


    private Integer taskId;

    private String tableName;

    private Integer dataItemCount;

    private Integer status;

    private String createBy;

    private String createTime;

    private String updateBy;

    private String updateTime;

    private Integer delFlag;
}
