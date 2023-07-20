package com.lrhealth.data.converge.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * XDS信息
 *
 * @author lr
 * @since 2023-07-19
 */
@Data
public class ConvFileInfoDto implements Serializable {
    /**
     * 主键id
     */
    private Long id;
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
}