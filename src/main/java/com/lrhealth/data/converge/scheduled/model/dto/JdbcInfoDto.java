package com.lrhealth.data.converge.scheduled.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jinmengyu
 * @date 2023-09-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JdbcInfoDto {

    /**
     * jdbc链接地址
     */
    private String jdbcUrl;

    /**
     * 数据库用户名
     */
    private String dbUserName;

    /**
     * 数据库密码
     */
    private String dbPasswd;

    /**
     * 库表采集范围和sql查询语句
     */
    private List<TableInfoDto> tableInfoDtoList;
}
