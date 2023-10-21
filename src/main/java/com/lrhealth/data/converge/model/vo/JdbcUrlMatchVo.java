package com.lrhealth.data.converge.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @author jinmengyu
 * @date 2023-09-19
 */
@Data
public class JdbcUrlMatchVo {

    private String host;

    private String port;

    private String database;

    private String schemaName;

    private List<String> paramsList;

    private String sid;

    private String serviceName;

}
