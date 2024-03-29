package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("std_original_model")
public class StdOriginalModel {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long catalogId;
    private String nameCn;
    private String nameEn;
    @TableLogic
    private Integer delFlag;
    private Date createTime;
    private String updateBy;
    private Date updateTime;
    private String description;
    private String orgCode;
    private String sysCode;
    private String dataType;
    private String modelQuerySql;
    private String convDsConfId;

}
