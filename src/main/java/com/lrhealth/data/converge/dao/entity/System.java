package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 系统表
 * </p>
 *
 * @author jinmengyu
 * @since 2023-07-20
 */
@Data
@TableName("conv_system")
public class System implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 系统编码
     */
    private String systemCode;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 逻辑删除字段，0-表示有效，1-表示删除
     */
    private Short delFlag;

    /**
     * 前置机交互文件类型
     */
    private String fileType;

    /**
     * 推送kafka主题
     */
    private String topic;

    /**
     * 使用状态
     */
    private String state;

    /**
     * 压缩方式
     */
    private String compressionWay;

    /**
     * 加密方式
     */
    private String encryptionWay;

    /**
     * 目标地址
     */
    private String targetAddress;

    /**
     * 汇聚类型
     */
    private String convergeType;

    /**
     * 采集方式
     */
    private String collectWay;

    /**
     * 系统厂商
     */
    private String systemFirm;

    /**
     * 标识是医保还是商保, 1-医保，2-商保
     */
    private Short sourceType;

    /**
     * 基础数据文件id
     */
    private Long baseDataId;

    /**
     * 结构文档文件id
     */
    private Long structureDocId;

    /**
     * 样例数据文件id
     */
    private Long sampleDataId;

    /**
     * 一级分类
     */
    private String oneLevelType;

    /**
     * 二级分类
     */
    private String twoLevelType;
}
