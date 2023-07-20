package com.lrhealth.data.converge.dao.entity;

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

    private Integer id;

    /**
     * 前置机系统机构关系
     */
    private String itemId;

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
