package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 客户端库表-原始模型字段映射
 *
 * @author admin
 * @TableName conv_original_column_map
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "conv_original_column_map")
public class ConvOriginalColumnMap implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 系统编码
     */
    private String sysCode;

    /**
     * 客户端表结构字段id(conv_original_column)
     */
    private Long oriTableColumnId;

    /**
     * 原始模型字段id(std_original_model_column)
     */
    private Long oriModelColumnId;

    /**
     * 原始模型字段名称
     */
    private String columnName;

    /**
     * 原始模型字段描述
     */
    private String columnDescription;

    /**
     * 原始模型字段数据类型
     */
    private String columnFieldType;

    /**
     * 原始模型字段数据长度
     */
    private Integer columnFieldLength;

    /**
     * 客户端表结构原始字段标识:1-是 0-否
     */
    private String oriColumnFlag;

    /**
     * 映射规则
     */
    private String mappingRule;

    /**
     * 逻辑删除标志(0:未删除;非0:已删除)
     */
    private Integer delFlag;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

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
        ConvOriginalColumnMap other = (ConvOriginalColumnMap) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getOrgCode() == null ? other.getOrgCode() == null : this.getOrgCode().equals(other.getOrgCode()))
                && (this.getSysCode() == null ? other.getSysCode() == null : this.getSysCode().equals(other.getSysCode()))
                && (this.getOriTableColumnId() == null ? other.getOriTableColumnId() == null : this.getOriTableColumnId().equals(other.getOriTableColumnId()))
                && (this.getOriModelColumnId() == null ? other.getOriModelColumnId() == null : this.getOriModelColumnId().equals(other.getOriModelColumnId()))
                && (this.getColumnName() == null ? other.getColumnName() == null : this.getColumnName().equals(other.getColumnName()))
                && (this.getColumnDescription() == null ? other.getColumnDescription() == null : this.getColumnDescription().equals(other.getColumnDescription()))
                && (this.getColumnFieldType() == null ? other.getColumnFieldType() == null : this.getColumnFieldType().equals(other.getColumnFieldType()))
                && (this.getColumnFieldLength() == null ? other.getColumnFieldLength() == null : this.getColumnFieldLength().equals(other.getColumnFieldLength()))
                && (this.getOriColumnFlag() == null ? other.getOriColumnFlag() == null : this.getOriColumnFlag().equals(other.getOriColumnFlag()))
                && (this.getMappingRule() == null ? other.getMappingRule() == null : this.getMappingRule().equals(other.getMappingRule()))
                && (this.getDelFlag() == null ? other.getDelFlag() == null : this.getDelFlag().equals(other.getDelFlag()))
                && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
                && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getOrgCode() == null) ? 0 : getOrgCode().hashCode());
        result = prime * result + ((getSysCode() == null) ? 0 : getSysCode().hashCode());
        result = prime * result + ((getOriTableColumnId() == null) ? 0 : getOriTableColumnId().hashCode());
        result = prime * result + ((getOriModelColumnId() == null) ? 0 : getOriModelColumnId().hashCode());
        result = prime * result + ((getColumnName() == null) ? 0 : getColumnName().hashCode());
        result = prime * result + ((getColumnDescription() == null) ? 0 : getColumnDescription().hashCode());
        result = prime * result + ((getColumnFieldType() == null) ? 0 : getColumnFieldType().hashCode());
        result = prime * result + ((getColumnFieldLength() == null) ? 0 : getColumnFieldLength().hashCode());
        result = prime * result + ((getOriColumnFlag() == null) ? 0 : getOriColumnFlag().hashCode());
        result = prime * result + ((getMappingRule() == null) ? 0 : getMappingRule().hashCode());
        result = prime * result + ((getDelFlag() == null) ? 0 : getDelFlag().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
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
        sb.append(", orgCode=").append(orgCode);
        sb.append(", sysCode=").append(sysCode);
        sb.append(", oriTableColumnId=").append(oriTableColumnId);
        sb.append(", oriModelColumnId=").append(oriModelColumnId);
        sb.append(", columnName=").append(columnName);
        sb.append(", columnDescription=").append(columnDescription);
        sb.append(", columnFieldType=").append(columnFieldType);
        sb.append(", columnFieldLength=").append(columnFieldLength);
        sb.append(", oriColumnFlag=").append(oriColumnFlag);
        sb.append(", mappingRule=").append(mappingRule);
        sb.append(", delFlag=").append(delFlag);
        sb.append(", createBy=").append(createBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}