package com.lrhealth.data.converge.model.dto;

import lombok.Data;

/**
 * @Author lei
 * @Date: 2023/12/25/14:21
 */
@Data
public class DataSourceConnectDTO {
    /**
     * 数据库ip地址
     */
    private String ip;
    /**
     * 数据库端口
     */
    private String port;
    /**
     * 数据库名称
     */
    private String dbName;
    /**
     * 用户名
     */
    private String dsUserName;
    /**
     * 密码
     */
    private String dsPwd;
    /**
     * 模式
     */
    private String schema;

    /**
     * 数据源url
     */
    private String dsUrl;
    /**
     * 数据源类型 1-平台数据源 2-客户数据源
     */
    private String dsType;
}
