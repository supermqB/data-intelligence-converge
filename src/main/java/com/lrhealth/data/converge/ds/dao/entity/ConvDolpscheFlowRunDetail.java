package com.lrhealth.data.converge.ds.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * ds工作流运行详情
 * </p>
 *
 * @author zhouyun
 * @since 2024-01-17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("conv_dolpsche_flow_run_detail")
public class ConvDolpscheFlowRunDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 汇聚侧-机构编码
     */
    private String orgCode;

    /**
     * ds侧-项目code
     */
    private String projectCode;

    /**
     * ds侧-项目名称
     */
    private String projectName;

    /**
     * ds侧-工作流code
     */
    private String flowCode;

    /**
     * ds侧-工作流名称
     */
    private String flowName;

    /**
     * ds侧-工作流实例id
     */
    private Integer flowInstanceId;

    /**
     * ds侧-工作流实例名称
     */
    private String flowInstanceName;

    /**
     * ds侧-工作流实例运行状态
     */
    private Integer flowInstanceStatus;

    /**
     * ds侧-工作流实例开始时间
     */
    private Date flowInstanceStartTime;

    /**
     * ds侧-工作流实例结束时间
     */
    private Date flowInstanceEndTime;

    /**
     * 删除标识 0 正常；1 删除
     */
    private Integer delFlag;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

}
