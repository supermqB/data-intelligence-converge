package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

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
@TableName("conv_fe_node")
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
    private Date createTime;

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
     */
    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Integer delFlag;

    /**
     * 前置机端口
     */
    private Integer port;

    /**
     * 系统编码
     */
    private String sysCode;

    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 上下线状态， 0-离线，1-在线
     */
    private Integer state;

    /**
     * 心跳时间
     */
    private LocalDateTime heartBeatTime;


    /**
     * 主键id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * aesKey
     */
    private String aesKey;

    /**
     * 网络情况:0-双向互通,1-单向访问前置机，2-单向访问中心端
     */
    private Integer network;
}
