package com.lrhealth.data.converge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2023-08-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataXExecDTO {
    /**
     *汇聚方式：1-接口模式，2-文件模式，3-队列模式，4-日志模式，5-视图模式，6-库表模式，7-自主模式
     */
    private String convergeMethod;
    /**
     * 机构编码
     */
    private String orgCode;
    /**
     * 系统编码
     */
    private String sysCode;
    /**
     *前置机ip地址
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
     * 前置机密码
     */
    private String frontendPwd;
    /**
     * 前置机文件存储地址
     */
    private String oriFilePath;
    /**
     * 0-不压缩，1-压缩
     */
    private String zipFlag;
    /**
     * 加密类型：0-不加密，1-AES，2-DES，3-RSA，4-SM2，5-SM4
     */
    private String encryptionWay;

    /**
     * xds主键id
     */
    private Long xdsId;

    /**
     * ODS模型名称
     * dataX的json配置文件名称
     */
    private String odsModelName;

    /**
     * 汇聚使用的文件存储目录
     */
    private String storedFilePath;

}
