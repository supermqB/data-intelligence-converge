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
     * 增量字段类型
     */
    private String incrFieldType;

    /**
     * 最新增量序列
     */
    private String latestSeq;

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

    // 表模型id
    private Long modelId;

    // 字段模型id
    private Long columnId;

    // 时间起始点位
    private String timeStartPoint;

    // 时间结束点位
    private String timeEndPoint;

    // 数字起始点位
    private String seqStartPoint;

    private String seqEndPoint;
}
