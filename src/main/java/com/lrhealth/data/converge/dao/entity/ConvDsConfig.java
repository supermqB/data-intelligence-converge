package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * ods数据源配置表
 * </p>
 *
 * @author zhouyun
 * @since 2023-12-22
 */
@Data
@TableName("conv_ds_config")
public class ConvDsConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 系统编码
     */
    private String sysCode;

    /**
     * 数据源-名称
     */
    private String dsName;

    /**
     * 数据源-类型  1：平台数据源  2：客户数据源
     */
    private Short dsType;

    /**
     * 数据源-驱动名称
     */
    private String dsDriverName;

    /**
     * 数据源-url
     */
    private String dsUrl;

    /**
     * 数据源-用户名
     */
    private String dsUsername;

    /**
     * 数据源-密码
     */
    private String dsPwd;

    /**
     * 数据源-连接池配置，json格式
     */
    private String dsPoolConfig;

    /**
     * 1-已删除 0-正常
     */
    private Short delFlag;

    private String schema;

    private String dbType;
    private String dbIp;
    private Integer dbPort;

    private String dbName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime heartBeatTime;

    private Integer defaultFlag;

    private String dsUrlForFront;

    private String domain;

    private String hdfsCluster;
}
