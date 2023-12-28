package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2023-08-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
