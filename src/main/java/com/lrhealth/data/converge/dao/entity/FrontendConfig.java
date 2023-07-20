package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 前置机配置信息
 * </p>
 *
 * @author jinmengyu
 * @since 2023-07-20
 */
@Data
@TableName("conv_frontend_config")
public class FrontendConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     *  前置机编码
     */
    private String frontendId;

    /**
     * 收集文件类型id
     */
    private String collectTypeId;

    /**
     * 采集文件类别：1-字典数据，2-基础数据，3-报告文书，4-影像文件，5-业务数据
     */
    private String collectType;

    /**
     * 文件位置
     */
    private String filePath;

    /**
     * 加密类型：0-不加密，1-AES，2-DES，3-RSA，4-SM2，5-SM4
     */
    private String encryptionWay;

    /**
     * 0-不压缩，1-压缩
     */
    private String zipFlag;

    /**
     * 定时策略
     */
    private String cron;

    /**
     * 工作流编码
     */
    private Long processDefinitionCode;

    /**
     * 定时工作流
     */
    private Long cronProcessDefinitionCode;

    /**
     * 数据库IP
     */
    private String serverIp;

    /**
     * 数据库端口
     */
    private String serverPort;

    /**
     * 数据库用户名
     */
    private String serverUser;

    /**
     * 数据库密码
     */
    private String serverPwd;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 逻辑删除字段，0-表示有效，1-表示删除
     */
    private Integer deleted;
}
