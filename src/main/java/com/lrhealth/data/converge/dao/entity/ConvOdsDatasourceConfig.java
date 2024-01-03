package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * <p>
 * ods数据源配置表
 * </p>
 *
 * @author zhouyun
 * @since 2023-12-22
 */
@TableName("conv_ds_config")
public class ConvOdsDatasourceConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 系统编码
     */
    private String sysCode;

    /**
     * 数据源-名称
     */
    private String dsName;

    /**
     * 数据源-类型  1：平台数据源  2：客户数据源 
     */
    private Short dsType;

    /**
     * 数据源-驱动名称
     */
    private String dsDriverName;

    /**
     * 数据源-url
     */
    private String dsUrl;

    /**
     * 数据源-用户名
     */
    private String dsUsername;

    /**
     * 数据源-密码
     */
    private String dsPwd;

    /**
     * 数据源-连接池配置，json格式
     */
    private String dsPoolConfig;

    /**
     * 1-已删除 0-正常
     */
    private Short delFlag;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getSysCode() {
        return sysCode;
    }

    public void setSysCode(String sysCode) {
        this.sysCode = sysCode;
    }

    public String getDsName() {
        return dsName;
    }

    public void setDsName(String dsName) {
        this.dsName = dsName;
    }

    public Short getDsType() {
        return dsType;
    }

    public void setDsType(Short dsType) {
        this.dsType = dsType;
    }

    public String getDsDriverName() {
        return dsDriverName;
    }

    public void setDsDriverName(String dsDriverName) {
        this.dsDriverName = dsDriverName;
    }

    public String getDsUrl() {
        return dsUrl;
    }

    public void setDsUrl(String dsUrl) {
        this.dsUrl = dsUrl;
    }

    public String getDsUsername() {
        return dsUsername;
    }

    public void setDsUsername(String dsUsername) {
        this.dsUsername = dsUsername;
    }

    public String getDsPwd() {
        return dsPwd;
    }

    public void setDsPwd(String dsPwd) {
        this.dsPwd = dsPwd;
    }

    public String getDsPoolConfig() {
        return dsPoolConfig;
    }

    public void setDsPoolConfig(String dsPoolConfig) {
        this.dsPoolConfig = dsPoolConfig;
    }

    public Short getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Short delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return "ConvOdsDatasourceConfig{" +
            "id = " + id +
            ", orgCode = " + orgCode +
            ", sysCode = " + sysCode +
            ", dsName = " + dsName +
            ", dsType = " + dsType +
            ", dsDriverName = " + dsDriverName +
            ", dsUrl = " + dsUrl +
            ", dsUsername = " + dsUsername +
            ", dsPwd = " + dsPwd +
            ", dsPoolConfig = " + dsPoolConfig +
            ", delFlag = " + delFlag +
        "}";
    }
}
