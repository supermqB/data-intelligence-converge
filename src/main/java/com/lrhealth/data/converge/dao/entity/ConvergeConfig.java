package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 汇聚配置
 * </p>
 *
 * @author jinmengyu
 * @since 2023-07-20
 */
@Data
@TableName("conv_converge_config")
public class ConvergeConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 系统编码
     */
    private String sysCode;

    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 前置机编码
     */
    private String frontendCode;

    /**
     * 配置项名称
     */
    private String convergeName;

    /**
     * 采集数据类别：1-字典数据，2-基础数据，3-报告文书，4-影像文件，5-业务数据
     *
     * @see com.lrhealth.data.common.enums.conv.CollectDataTypeEnum
     */
    private String dataType;

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
     * 汇聚方式：1-客户写文件，2-客户掉接口，3-平台调接口，4-平台读队列，5-平台读视图，6-平台读库表，7-自主模式
     *
     * @see com.lrhealth.data.common.enums.conv.ConvMethodEnum
     */
    private String convergeMethod;
    /**
     * 汇聚模式：0-直连模式 1-前置机中转模式
     *
     * @see com.lrhealth.data.common.enums.conv.ConvModeEnum
     */
    private String convergeMode;

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
     *
     * @see com.lrhealth.data.common.enums.conv.LogicDelFlagEnum
     */
    private Integer delFlag;
}
