package com.lrhealth.data.converge.ds.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 汇聚和ds关系表
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
@TableName("conv_dolpsche_rel")
public class ConvDolpscheRel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 汇聚侧-机构编码
     */
    private String convOrgCode;

    /**
     * ds侧-项目编码
     */
    private String dsProjectCode;

    /**
     * ds侧-工作流编码
     */
    private String dsFlowCode;

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

}
