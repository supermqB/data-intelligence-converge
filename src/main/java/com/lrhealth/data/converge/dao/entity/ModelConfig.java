package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * 模型配置
 * @author admin
 */
@TableName("model_config")
public class ModelConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 表自增id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 模型id
     */
    private Long modelId;
    /**
     * 表名称
     */
    private String tableName;
    /**
     * 表描述
     */
    private String tableDesc;
    /**
     * 表类型
     */
    private String tableType;
    /**
     * 表配置
     */
    private String dbConfig;
    /**
     * 表配置
     */
    private Date createTime;
    /**
     * 删除标志
     */
    private Integer delFlag;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableDesc() {
        return tableDesc;
    }

    public void setTableDesc(String tableDesc) {
        this.tableDesc = tableDesc;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getDbConfig() {
        return dbConfig;
    }

    public void setDbConfig(String dbConfig) {
        this.dbConfig = dbConfig;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }
}
