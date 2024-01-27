package com.lrhealth.data.converge.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 汇聚监测配置表
 *
 * @TableName conv_monitor_conf
 */
@TableName(value = "conv_monitor_conf")
@Data
public class ConvMonitorConf implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 异常发生时间
     */
    private Date exceptionTime;

    /**
     * 前置机ID
     */
    private Long convFeNodeId;

    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 监控类型：1 前置机程序状态 2 批量采集任务执行 3 CDC程序状态监测  4 CDC采集任务执行  5 镜像数据库连接检查  6 中心数据库连接检查
     */
    private String monitorType;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 配置类型：1-通用配置 2-指定配置
     */
    private String confType;

    /**
     * 数据源ID，关联conv_ds_conf表ID
     */
    private Integer convDsId;

    /**
     * 管道ID，关联conv_tunnel表ID
     */
    private Long convTunnelId;

    /**
     * 监控开关：0-关闭 1-打开
     */
    private String monitorSwitch;

    /**
     * 监控时间间隔
     */
    private Integer monitorInterval;

    /**
     * 监控时间间隔单位 0-秒 1-分钟 2-小时 3-天
     */
    private String monitorIntervalUnit;

    /**
     * 异常时间间隔
     */
    private Integer errorInterval;

    /**
     * 异常时间间隔单位 0-秒 1-分钟 2-小时 3-天
     */
    private String errorIntervalUnit;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}