package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("std_original_model")
public class StdOriginalModel {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long catalogId;
    private String nameCn;
    private String nameEn;
    @TableLogic
    private Integer delFlag;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private String description;
    private String orgCode;
    private String sysCode;

    /**
     * 原始结构主键id
     */
    private Long originalId;
    /**
     * 表创建标志 0-未创建表 1-已创建表
     */
    private Long tableCreateFlag;
    /**
     * 数据源Id
     */
    private Integer convDsConfId;

    private String dataType;

    private String modelQuerySql;
    /**
     * 文件存储路径(hive)
     */
    private String storagePath;
}
