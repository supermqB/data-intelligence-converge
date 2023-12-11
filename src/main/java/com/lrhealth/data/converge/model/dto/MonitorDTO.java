package com.lrhealth.data.converge.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 监控信息
 * @author admin
 */
@Data
public class MonitorDTO implements Serializable {
    /**
     * 异常主键id
     */
    private Long id;
    /**
     * 状态 0-正常 1-异常
     */
    private Boolean status;
    /**
     * 更新时间
     */
    private Date updateTime;
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
     * 监控类型
     */
    private String monitorType;
}
