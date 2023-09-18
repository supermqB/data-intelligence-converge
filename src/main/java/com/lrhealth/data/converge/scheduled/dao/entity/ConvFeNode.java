package com.lrhealth.data.converge.scheduled.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 前置机信息
 * </p>
 *
 * @author zhangguowen-generator
 * @since 2023-09-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("di_conv_fe_node")
public class ConvFeNode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 前置机名称
     */
    private String name;

    /**
     * 前置机ip地址
     */
    private String ip;

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
    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Integer delFlag;

    /**
     * 前置机端口
     */
    private Integer port;

    /**
     * 前置机用户名
     */
    private String username;

    /**
     * 前置机密码
     */
    private String passwd;

    /**
     * 系统编码
     */
    private String sysCode;

    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 上下线状态
     */
    private String state;


    /**
     * 主键id
     */
    private Long id;


}
