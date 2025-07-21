package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvDsConfDTO {
    /**
     * 主键
     */
    private Long id;
    /**
     * 机构编码
     */
    private String orgCode;
    /**
     * 数据源id
     */
    private Integer dsId;

    /**
     * 数据源类型
     */
    private Short dsType;
    /**
     * 数据源url
     */
    private String dsUrl;
    /**
     * 数据源名称
     */
    private String dsUsername;
    /**
     * 密码
     */
    private String dsPwd;
    /**
     * 密码
     */
    private String dbType;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 操作 save delete update
     */
    private String operate;

    private List<Integer> delIds;

}
