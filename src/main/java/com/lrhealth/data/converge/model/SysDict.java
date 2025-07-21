package com.lrhealth.data.converge.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统字典表
 * @TableName sys_dict
 */
@TableName(value ="sys_dict")
@Data
public class SysDict implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 字典编码
     */
    private String dictCode;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典项值
     */
    private String dictItemValue;

    /**
     * 字典项描述
     */
    private String dictItemDesc;

    /**
     * 默认值标识 0 不是默认值 1 是默认值
     */
    private Integer defaultFlag;

    /**
     * 删除标识 0 正常；1 删除
     */
    private Integer delFlag;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        SysDict other = (SysDict) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getDictCode() == null ? other.getDictCode() == null : this.getDictCode().equals(other.getDictCode()))
            && (this.getDictName() == null ? other.getDictName() == null : this.getDictName().equals(other.getDictName()))
            && (this.getDictItemValue() == null ? other.getDictItemValue() == null : this.getDictItemValue().equals(other.getDictItemValue()))
            && (this.getDictItemDesc() == null ? other.getDictItemDesc() == null : this.getDictItemDesc().equals(other.getDictItemDesc()))
            && (this.getDefaultFlag() == null ? other.getDefaultFlag() == null : this.getDefaultFlag().equals(other.getDefaultFlag()))
            && (this.getDelFlag() == null ? other.getDelFlag() == null : this.getDelFlag().equals(other.getDelFlag()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getDictCode() == null) ? 0 : getDictCode().hashCode());
        result = prime * result + ((getDictName() == null) ? 0 : getDictName().hashCode());
        result = prime * result + ((getDictItemValue() == null) ? 0 : getDictItemValue().hashCode());
        result = prime * result + ((getDictItemDesc() == null) ? 0 : getDictItemDesc().hashCode());
        result = prime * result + ((getDefaultFlag() == null) ? 0 : getDefaultFlag().hashCode());
        result = prime * result + ((getDelFlag() == null) ? 0 : getDelFlag().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", dictCode=").append(dictCode);
        sb.append(", dictName=").append(dictName);
        sb.append(", dictItemValue=").append(dictItemValue);
        sb.append(", dictItemDesc=").append(dictItemDesc);
        sb.append(", defaultFlag=").append(defaultFlag);
        sb.append(", delFlag=").append(delFlag);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}