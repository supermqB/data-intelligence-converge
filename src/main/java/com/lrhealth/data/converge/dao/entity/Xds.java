package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * XDS信息
 *
 * @author lr
 * @since 2023-07-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("conv_xds")
public class Xds implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 业务流水号
     */
    private String serialNum;
    /**
     * 机构编码
     */
    private String orgCode;
    /**
     * 机构名称
     */
    private String orgName;
    /**
     * 系统编码
     */
    private String sysCode;
    /**
     * 医院编码（发生地编码）
     */
    private String hpsCode;
    /**
     * 任务实例ID（任务队列系统）
     */
    private String taskInstanceId;
    /**
     * 任务实例名称（任务队列系统）
     */
    private String taskInstanceName;
    /**
     * 汇聚方式：1-客户写文件;2-客户掉接口，3-平台调接口，4-平台读队列，5-平台读视图，6-平台读库表，7-自主模式
     *
     * @see com.lrhealth.data.common.enums.conv.ConvModeEnum
     */
    private String convergeMethod;
    /**
     * 数据类别：1-字典数据;2-基础数据，3-报告文书，4-影像文件，5-业务数据
     *
     * @see com.lrhealth.data.common.enums.conv.CollectDataTypeEnum
     */
    private String dataType;
    /**
     * 汇聚开始时间
     */
    private LocalDateTime dataConvergeStartTime;
    /**
     * 汇聚结束时间
     */
    private LocalDateTime dataConvergeEndTime;
    /**
     * 汇聚状态：0-初始化;1-完成 2-失败
     *
     * @see com.lrhealth.data.common.enums.conv.XdsStatusEnum
     */
    private Integer dataConvergeStatus;
    /**
     * 数据汇聚失败描述
     */
    private String dataConvergeDesc;
    /**
     * 文件记录IP地址
     */
    private String oriFileFromIp;
    /**
     * 文件类型
     */
    private String oriFileType;
    /**
     * 原文件名称（数据库采集为表名称）
     */
    private String oriFileName;
    /**
     * 原文件大小（默认单位为B）
     */
    private BigDecimal oriFileSize;
    /**
     * 文件存储名称
     */
    private String storedFileName;
    /**
     * 文件导入目标系统类型
     */
    private String storedFileType;
    /**
     * 存储文件方式：0-本地存储;1-Ceph存储
     *
     * @see com.lrhealth.data.common.enums.conv.XdsStoredFileModeEnum
     */
    private Integer storedFileMode;
    /**
     * 文件存储地址
     */
    private String storedFilePath;
    /**
     * 存储文件大小（默认单位为B）
     */
    private BigDecimal storedFileSize;
    /**
     * 发送kafka消息结果：0-未发送;1-已发送
     *
     * @see com.lrhealth.data.common.enums.conv.KafkaSendFlagEnum
     */
    private Integer kafkaSendFlag;
    /**
     * 数据抽取批次号
     */
    private String batchNo;
    /**
     * ODS模型名称
     */
    private String odsModelName;
    /**
     * ODS数据库表名称
     */
    private String odsTableName;
    /**
     * ODS数据库落库数据条数
     */
    private Integer dataCount;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建日期
     */
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新日期
     */
    private LocalDateTime updateTime;
    /**
     * 逻辑删除字段;0-表示有效，1-表示删除
     *
     * @see com.lrhealth.data.common.enums.conv.LogicDelFlagEnum
     */
    private Integer delFlag;

}