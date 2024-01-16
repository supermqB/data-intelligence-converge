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
 * 客户端库表-原始模型映射
 *
 * @author admin
 * @TableName conv_original_table_map
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "conv_original_table_map")
public class ConvOriginalTableMap implements Serializable {
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
     * 客户端表ID,关联conv_original_table表ID
     */
    private Long originalTableId;

    /**
     * 原始模型ID,关联std_original_model表ID
     */
    private Long originalModelId;

    /**
     * 原始模型名称
     */
    private String originalModelName;

    /**
     * 原始模型备注
     */
    private String originalModelDescription;

    /**
     * 逻辑删除标志(0:未删除;非0:已删除)
     */
    private Object delFlag;

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
        ConvOriginalTableMap other = (ConvOriginalTableMap) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getOrgCode() == null ? other.getOrgCode() == null : this.getOrgCode().equals(other.getOrgCode()))
                && (this.getSysCode() == null ? other.getSysCode() == null : this.getSysCode().equals(other.getSysCode()))
                && (this.getOriginalTableId() == null ? other.getOriginalTableId() == null : this.getOriginalTableId().equals(other.getOriginalTableId()))
                && (this.getOriginalModelId() == null ? other.getOriginalModelId() == null : this.getOriginalModelId().equals(other.getOriginalModelId()))
                && (this.getOriginalModelName() == null ? other.getOriginalModelName() == null : this.getOriginalModelName().equals(other.getOriginalModelName()))
                && (this.getOriginalModelDescription() == null ? other.getOriginalModelDescription() == null : this.getOriginalModelDescription().equals(other.getOriginalModelDescription()))
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
        result = prime * result + ((getOriginalTableId() == null) ? 0 : getOriginalTableId().hashCode());
        result = prime * result + ((getOriginalModelId() == null) ? 0 : getOriginalModelId().hashCode());
        result = prime * result + ((getOriginalModelName() == null) ? 0 : getOriginalModelName().hashCode());
        result = prime * result + ((getOriginalModelDescription() == null) ? 0 : getOriginalModelDescription().hashCode());
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
        sb.append(", originalTableId=").append(originalTableId);
        sb.append(", originalModelId=").append(originalModelId);
        sb.append(", originalModelName=").append(originalModelName);
        sb.append(", originalModelDescription=").append(originalModelDescription);
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