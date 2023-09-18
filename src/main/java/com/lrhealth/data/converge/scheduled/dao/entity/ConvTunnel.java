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
 * 汇聚方式配置信息
 * </p>
 *
 * @author zhangguowen-generator
 * @since 2023-09-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("di_conv_tunnel")
public class ConvTunnel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 前置机编码
     */
    private Long frontendId;


    /**
     * 0-不压缩，1-压缩
     */
    private String zipFlag;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
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
     * 汇聚方式：1-库表模式，2-日志模式（CDC），3-文件模式，4-接口模式，5-队列模式
     */
    private String convergeMethod;

    /**
     * 配置项名称
     */
    private String name;

    /**
     * 系统编码
     */
    private String sysCode;

    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 主键id
     */
    private Long id;

    /**
     * 数据库类型
     */
    private String dbType;

    /**
     * jdbc链接地址
     */
    private String jdbcUrl;

    /**
     * 用户名
     */
    private String dbUserName;

    /**
     * 密码
     */
    private String dbPasswd;

    /**
     * 采集范围
     */
    private String collectRange;

    /**
     * 任务调度cron表达式
     */
    private String cronStr;

    /**
     * 数据分片大小
     */
    private Long dataShardSize;

    /**
     * 是否加密：0-不加密，1-加密
     */
    private String encryptionFlag;

    /**
     * 管道状态：0-任务已排班，1-任务执行中，2-暂停，3-废弃
     */
    private Integer status;


}
