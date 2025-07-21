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
import java.util.Date;

/**
 * <p>
 * 客户端库表信息
 * </p>
 *
 * @author jinmengyu
 * @since 2024-01-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("conv_original_table")
public class ConvOriginalTable implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 表中文名
     */
    private String nameCn;

    /**
     * 表英文名
     */
    private String nameEn;

    /**
     * 备注信息
     */
    private String description;

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
     * 机构编码
     */
    private String orgCode;

    /**
     * 系统编码
     */
    private String sysCode;

    /**
     * 数据类型：1-字典数据 2-基础数据 3-报告文书 4-影像文件 5-业务数据
     */
    private String dataType;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 数据条数
     */
    private Long dataCount;

    /**
     * 数据源ID,关联conv_ds_config表ID
     */
    private Integer convDsConfId;

    /**
     * 是否已生成原始模型
     */
    private Long modelId;

    private Integer dataSource;

    /**
     * 原始模型名称
     */
    private String modelName;

    /**
     * 原始模型备注
     */
    private String modelDescription;

    private Long dataSize;

    private Date probeTime;
}
