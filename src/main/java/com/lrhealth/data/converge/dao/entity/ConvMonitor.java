package com.lrhealth.data.converge.dao.entity;

/**
 * 汇聚前置机监测表
 *
 * @author zhuanning
 * @since 2023-11-30
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("conv_monitor")
public class ConvMonitor  implements Serializable {


    private static final long serialVersionUID = 4237454223514096038L;
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 监控类型：1 前置机程序 2 FlinkCDC程序
     */
    private String monitorType;
    /**
     * 数据源ID
     */
    private Integer dsId;

    /**
     * 镜像库表名称
     */
    private String tableName;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 更新时间
     */
    private Date createTime;
}
