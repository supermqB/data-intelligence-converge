package com.lrhealth.data.converge.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2023-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DbValidVo {

    private String jdbcUrl;

    private String host;

    private String port;

    private String dbUserName;

    private String dbPassword;

    private String dbType;

}
