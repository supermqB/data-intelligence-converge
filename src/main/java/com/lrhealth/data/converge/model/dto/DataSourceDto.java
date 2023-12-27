package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2023-12-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceDto {

    private String jdbcUrl;

    private String username;

    private String password;

    private String driver;


}
