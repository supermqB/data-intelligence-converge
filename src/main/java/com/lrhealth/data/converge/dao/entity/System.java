package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 系统表
 * </p>
 *
 * @author jinmengyu
 * @since 2023-07-20
 */
@Data
@TableName("conv_system")
public class System implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 机构ID
     */
    private Long institutionId;
    /**
     * 机构编码
     */
    private String sourceCode;

    /**
     * 系统编码
     */
    private String systemCode;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 逻辑删除字段，0-表示有效，1-表示删除
     *
     * @see com.lrhealth.data.common.enums.conv.LogicDelFlagEnum
     */
    private Short delFlag;
    /**
     * 使用状态
     */
    private String state;
    /**
     * 系统厂商
     */
    private String systemFirm;
    /**
     * 一级分类
     */
    private String oneLevelType;

    /**
     * 二级分类
     */
    private String twoLevelType;
}
