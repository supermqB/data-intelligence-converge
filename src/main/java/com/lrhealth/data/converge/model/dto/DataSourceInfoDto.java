package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2024-01-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceInfoDto {

    private Integer dsConfId;

    private String orgCode;

    private String sysCode;

    private String dbType;

    private String jdbcUrl;

    private String username;

    private String password;

    private String schema;

    private Integer structure;

    private String driver;
}
