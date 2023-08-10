package com.lrhealth.data.converge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 前置机和配置项的文件相关配置信息
 * @author jinmengyu
 * @date 2023-07-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FepFileInfoVo {

    /**
     * 文件存储地址（文件中转使用）
     */
    private String oriFileFromPath;

    /**
     * 前置机ip地址
     */
    private String frontendIp;

    /**
     * 前置机端口
     */
    private String frontendPort;

    /**
     * 前置机用户名
     */
    private String frontendUsername;

    /**
     * 前置机ip密码
     */
    private String frontendPwd;

    /**
     * 文件存储位置(文档解析使用)
     */
    private String storedFilePath;

    /**
     * 加密类型：0-不加密，1-AES，2-DES，3-RSA，4-SM2，5-SM4
     *
     * @see com.lrhealth.data.common.enums.conv.EncryptionWayEnum
     */
    private String encryptionWay;

    /**
     * 0-不压缩，1-压缩
     *
     * @see com.lrhealth.data.common.enums.conv.ZipFlagEnum
     */
    private String zipFlag;

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
     * 系统编码
     */
    private String sysCode;

    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 采集数据类别：1-字典数据，2-基础数据，3-报告文书，4-影像文件，5-业务数据
     *
     * @see com.lrhealth.data.common.enums.conv.CollectDataTypeEnum
     */
    private String dataType;

    /**
     * 汇聚方式：1-接口模式，2-文件模式，3-队列模式，4-日志模式，5-视图模式，6-库表模式，7-自主模式
     *
     * @see com.lrhealth.data.common.enums.conv.ConvMethodEnum
     */
    private String convergeMethod;
}
