package com.lrhealth.data.converge.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 文件采集所需信息
 * 采集流程的统一入参
 * @author jinmengyu
 * @date 2023-08-25
 */
@Data
public class FileConvergeInfoDTO {

    /**
     * 文件存储地址（文件中转使用）
     */
    private String oriFilePath;
    /**
     * 原始文件名
     */
    private String oriFileName;
    /**
     * 原始文件类型
     */
    private String oriFileType;
    /**
     * 原始文件大小
     */
    private BigDecimal oriFileSize;
    /**
     * 前置机ip地址
     * 与oriFileFromIp相同
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

}
