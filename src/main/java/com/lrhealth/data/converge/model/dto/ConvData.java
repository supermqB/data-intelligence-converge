package com.lrhealth.data.converge.model.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * <p>
 * 采集数据
 * </p>
 *
 * @author lr
 * @since 2023/11/28 18:07
 */
@Data
public class ConvData {
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
    /**
     * 数据
     */
    private JSONObject value;
}
