package com.lrhealth.data.converge.dao.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("std_original_model_column")
public class StdOriginalModelColumn implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(
            value = "id",
            type = IdType.AUTO
    )
    private Long id;
    private Long modelId;
    private String nameCn;
    private String nameEn;
    private Integer seqNo;
    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Integer delFlag;
    private String createBy;
    @TableField(
            fill = FieldFill.INSERT
    )
    private LocalDateTime createTime;
    private String updateBy;
    @TableField(
            fill = FieldFill.INSERT_UPDATE
    )
    private LocalDateTime updateTime;
    private String elementType;
    private String elementFormat;
    private String definition;
    private String primaryKeyFlag;
    private String requiredFlag;
    private String sysCode;
    private String orgCode;
    private String businessTimeFlag;
    private String fieldType;
    private Integer fieldTypeLength;
    private String incrFlag;
}
