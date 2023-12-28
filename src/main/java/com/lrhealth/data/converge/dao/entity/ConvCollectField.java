package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author lei
 * @Date: 2023/11/30/17:47
 */

@Data
@TableName("conv_collect_field")
public class ConvCollectField {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 管道id
     */
    private Integer tunnelId;

    /**
     * 查询字段
     */
    private String columnField;

    /**
     * 系统编码
     */
    private String systemCode;

    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 条件字段
     */
    private String conditionField;

    private String tableName;

    private String querySql;

    private String storeTableName;
}
