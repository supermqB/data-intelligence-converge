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
 * 增量采集时间表
 * </p>
 *
 * @author jinmengyu
 * @since 2024-01-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("conv_collect_incr_time")
public class ConvCollectIncrTime implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 管道id
     */
    private Long tunnelId;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 增量字段
     */
    private String incrField;

    /**
     * 最新采集时间
     */
    private String latestTime;

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
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
}
