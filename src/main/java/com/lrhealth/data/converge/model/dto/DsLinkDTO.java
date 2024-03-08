package com.lrhealth.data.converge.model.dto;

import lombok.Data;

/**
 * @Author lei
 * @Date: 2023/12/25/14:21
 */
@Data
public class DsLinkDTO {
    /**
     * 机构编码
     */
    private String orgCode;
    /**
     * 数据源类型 1-平台数据源 2-客户数据源
     */
    private String dsType;
    /**
     * 用户名
     */
    private String dsUserName;
    /**
     * 密码
     */
    private String dsPwd;
    /**
     * 数据源url
     */
    private String dsUrl;
    /**
     * 数据源连接状态
     */
    private Boolean linkState;
}
