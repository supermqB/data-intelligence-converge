package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 前置机-项目关系表
 * </p>
 *
 * @author jinmengyu
 * @since 2023-07-20
 */
@Data
@TableName("conv_item_project_relation")
public class ItemProjectRelation implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * conv_sys_org_front_item表ID
     */
    private Long itemId;

    /**
     * 项目ID

     */
    private String projectId;

    /**
     * 创建时间
     */
    private LocalDateTime createDate;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateDate;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 逻辑删除字段，0-表示有效，1-表示删除
     */
    private Short deleted;
}
