package com.lrhealth.data.converge.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author jinmengyu
 * @date 2023-08-31
 */
@Data
@Builder
public class DataBaseMessageDTO {

    private String database;

    private String host;

    private String port;

    private String databaseName;

    private String schemaName;

    private String userName;

    private String userPassword;

    private String jdbcUrl;

    private String condition;

    private String jsonSavePath;


}
