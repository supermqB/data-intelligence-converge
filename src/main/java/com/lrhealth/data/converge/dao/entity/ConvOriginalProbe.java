package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author jinmengyu
 * @date 2025-04-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("conv_original_probe")
public class ConvOriginalProbe implements Serializable {

    private static final long serialVersionUID = 244353121L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer probeModel;

    private Long tableId;

    private String tableName;

    private Long columnId;

    private String columnName;

    private Double nullable;

    private String columnValue;

    private Double valueFreq;

    private Integer delFlag;

    private LocalDateTime createTime;
}
