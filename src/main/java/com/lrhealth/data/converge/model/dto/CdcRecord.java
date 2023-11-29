package com.lrhealth.data.converge.model.dto;

import lombok.Data;

import java.util.TreeMap;

/**
 * <p>
 * 采集数据
 * </p>
 *
 * @author lr
 * @since 2023/11/28 18:07
 */
@Data
public class CdcRecord {
    /**
     * 系统编码
     */
    private String systemCode;

    /**
     * 数据库民称
     */
    private String database;
    /**
     * 模式
     */
    private String schema;
    /**
     * 表名
     */
    private String table;
    /**
     * 操作:update,insert
     */
    private String operation;

    private TreeMap<String, Object> value;

    private String jid;
    private Long tunnelId;
    private Long taskId;
}
