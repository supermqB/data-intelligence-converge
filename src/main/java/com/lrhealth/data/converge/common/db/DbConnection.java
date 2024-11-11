package com.lrhealth.data.converge.common.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2024-11-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DbConnection {

    private String dbUrl;

    private String dbUserName;

    private String dbPassword;

    private String dbDriver;

}
