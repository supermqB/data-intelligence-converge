package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 客户端库表字段信息
 * </p>
 *
 * @author jinmengyu
 * @since 2024-01-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("conv_original_column")
public class ConvOriginalColumn implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联conv_original_table表id
     */
    private Long tableId;

    /**
     * 字段描述(字段中文名)
     */
    private String nameCn;

    /**
     * 字段名(字段英文名)
     */
    private String nameEn;

    /**
     * 排序号
     */
    private Integer seqNo;

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
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 字段定义
     */
    private String definition;

    /**
     * 主键标志(0-否 1-是)
     */
    private String primaryKeyFlag;

    /**
     * 必填标志(0-否 1-是)
     */
    private String requiredFlag;

    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 系统编码
     */
    private String sysCode;

    /**
     * 字段数据库数据类型
     */
    private String fieldType;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 字段类型长度
     */
    private Integer fieldTypeLength;

    /**
     * 增量标识(0-否 1-是)
     */
    private String incrFlag;

    /**
     * 是否业务时间(0-否 1-是)
     */
    private String businessTimeFlag;

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
     * 字段数据类型编号
     */
    private String dataType;

    private String elementFormat;

    private Integer probeTag;

    private String elementType;
}
