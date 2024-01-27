package com.lrhealth.data.converge.dao.entity;

/**
 * 汇聚前置机监测表
 *
 * @author zhuanning
 * @since 2023-11-30
 */

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("conv_monitor")
public class ConvMonitor  implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 上下线状态， 0-正常，1-异常
     */
    private Integer state;
    /**
     * 异常描述
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String exceptionDes;
    /**
     * 异常发生时间
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
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
     * 系统编码
     */
    private String sysCode;
    /**
     * 监控类型：1 前置机程序 2 FlinkCDC程序
     */
    private String monitorType;
    /**
     * 数据源ID
     */
    private Integer dsId;
    /**
     * 更新时间
     */
    private Date updateTime;
}
