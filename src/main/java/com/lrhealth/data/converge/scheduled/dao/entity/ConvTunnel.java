package com.lrhealth.data.converge.scheduled.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 汇聚方式配置信息
 * </p>
 *
 * @author zhangguowen-generator
 * @since 2023-09-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("conv_tunnel")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvTunnel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 前置机编码
     */
    private Long frontendId;


    /**
     * 库表采集-库到文件
     * 0-不压缩，1-压缩
     */
    private String zipFlag;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 逻辑删除字段，0-表示有效，1-表示删除
     */
    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Integer delFlag;

    /**
     * 汇聚方式：1-库表模式，2-日志模式（CDC），3-文件模式，4-接口模式，5-队列模式
     */
    private String convergeMethod;

    /**
     * 配置项名称
     */
    private String name;

    /**
     * 系统编码
     */
    private String sysCode;

    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 主键id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 读库的的数据库类型
     */
    private String dbType;

    /**
     * 读库的jdbc链接地址
     */
    private String jdbcUrl;

    /**
     * 读库的用户名
     */
    private String dbUserName;

    /**
     * 读库的密码
     */
    private String dbPasswd;

    /**
     * 采集范围（读库的表名称）
     */
    private String collectRange;

    /**
     * 任务调度cron表达式
     */
    private String cronStr;

    /**
     * 库表采集-库到文件
     * 数据分片大小
     */
    private Long dataShardSize;

    /**
     * 库表采集-库到文件
     * 文件是否加密：0-不加密，1-加密
     */
    private String encryptionFlag;

    /**
     * 管道状态：0-待配置，1-任务已排班，2-任务执行中，3-暂停，4-废弃
     */
    private Integer status;

    /**
     * 文件模式
     * 扫描目录
     */
    private String fileModeCollectDir;

    /**
     * 队列模式
     * 消息主题
     */
    private String mqModeTopicName;

    /**
     * 结构化数据标识
     * 1-结构化数据，2-非结构化数据
     */
    private Integer structuredDataFlag;

    /**
     * 库表采集模式
     * 0-库到文件 1-库到库
     */
    private Integer collectModel;

    /**
     * 采集类型
     * 1-全量采集 2-增量采集
     */
    private Integer colType;

    /**
     * 库表采集-全量采集
     * 全量采集开始时间
     */
    private LocalDateTime fullColStartTime;

    /**
     * 库表采集-全量采集
     * 全量采集结束时间
     */
    private LocalDateTime fullColEndTime;


    /**
     * 库表采集-库到库
     * 写库的jdbc链接地址
     */
    private String jdbcUrlForIn;

    /**
     * 库表采集-库到库
     * 写库的用户名
     */
    private String dbUserNameForIn;

    /**
     * 库表采集-库到库
     * 写库的密码
     */
    private String dbPasswdForIn;
}
