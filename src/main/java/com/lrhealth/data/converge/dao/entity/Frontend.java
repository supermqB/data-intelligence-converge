package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 前置机信息
 * </p>
 *
 * @author jinmengyu
 * @since 2023-07-20
 */
@Data
@TableName("conv_frontend")
public class Frontend implements Serializable {
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
     * 文件存储地址（文件中转使用）
     */
    private String filePath;
    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 前置机编码
     */
    private String frontendCode;

    /**
     * 前置机名称
     */
    private String frontendName;

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
     * vpn
     */
    private String vpn;

    /**
     * vpn-ip地址
     */
    private String vpnIp;


    /**
     * vpn-端口
     */
    private String vpnPort;


    /**
     * vpn-用户
     */
    private String vpnUser;


    /**
     * vpn-密码
     */
    private String vpnPwd;

    /**
     * 创建时间
     */
    private LocalDateTime createDate;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateDate;

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

    /**
     * 上下线状态
     *
     * @see com.lrhealth.data.common.enums.conv.OnlineStateEnum
     */
    private String state;
}
