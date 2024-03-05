package com.lrhealth.data.converge.model.dto;

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
    private Integer dsId;
    /**
     * 读库的jdbc链接地址
     */
    private String jdbcUrl;

    /**
     * 读库的用户名
     */
    private String dbUserName;

    /**
     * 读库的密码
     */
    private String dbPasswd;

    /**
     * 读库的模式
     */
    private String dbSchema;

    /**
     * 采集模式
     * 0-库到文件 1-库到库
     */
    private Integer collectModel;

    /**
     * 采集类型
     * 1-全量采集 2-增量采集
     */
    private Integer colType;

    /**
     * 库表采集-全量采集
     * 全量采集开始时间
     */
    private String fullColStartTime;

    /**
     * 库表采集-全量采集
     * 全量采集结束时间
     */
    private String fullColEndTime;


    /**
     * 库表采集-库到库
     * 写库的jdbc链接地址
     */
    private String jdbcUrlForIn;

    /**
     * 库表采集-库到库
     * 写库的用户名
     */
    private String dbUserNameForIn;

    /**
     * 库表采集-库到库
     * 写库的密码
     */
    private String dbPasswdForIn;

    /**
     * 库表采集范围和sql查询语句
     */
    private List<TableInfoDto> tableInfoDtoList;

    /**
     * 1-单表采集 2-关联采集
     */
    private Integer colTableType;

    private Integer dsConfigId;

}
