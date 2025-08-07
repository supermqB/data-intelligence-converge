package com.lrhealth.data.converge.model.fep;

import lombok.Data;

/**
 * @author jinmengyu
 * @date 2025-04-21
 */
@Data
public class WriterConfigDTO {

    private String dbType;

    private String jdbcUrl;

    private String username;

    private String password;

    private String driverName;
}
