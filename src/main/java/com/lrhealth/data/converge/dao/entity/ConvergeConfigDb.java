package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 汇聚配置
 * </p>
 *
 * @author jinmengyu
 * @since 2023-07-20
 */
@Data
@TableName("conv_converge_config_db")
public class ConvergeConfigDb implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * conv_converge_config表ID
     */
    private Long convConfId;
    /**
     * 数据库类型
     */
    private String databaseType;
    /**
     * 数据库名称
     */
    private String databaseName;
    /**
     * 数据库Schema
     */
    private String databaseSchema;
    /**
     * 数据库IP
     */
    private String databaseIp;

    /**
     * 数据库端口
     */
    private String databasePort;

    /**
     * 数据库用户名
     */
    private String databaseUser;

    /**
     * 数据库密码
     */
    private String databasePwd;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 逻辑删除字段，0-表示有效，1-表示删除
     *
     * @see com.lrhealth.data.common.enums.conv.LogicDelFlagEnum
     */
    private Integer delFlag;
}
