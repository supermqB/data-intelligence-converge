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
 * 数据源字段类型映射
 * </p>
 *
 * @author flzhang
 * @since 2024-01-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("conv_field_type")
public class ConvFieldType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 客户端数据源
     */
    private String clientSource;

    /**
     * 客户端字段类型
     */
    private String clientFieldType;

    /**
     * 平台数据源
     */
    private String platformSource;

    /**
     * 平台字段类型
     */
    private String platformFieldType;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String clientDataType;

    private String platformDataType;
}
