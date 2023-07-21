package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 合作机构表
 * </p>
 *
 * @author jinmengyu
 * @since 2023-07-20
 */
@Data
@TableName("conv_institution")
public class Institution implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 机构编码
     */
    private String sourceCode;

    /**
     * 机构名称
     */
    private String sourceName;

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
     * 逻辑删除字段， 0-未删除,1-已删除
     * @see com.lrhealth.data.common.enums.conv.LogicDelFlagEnum
     */
    private Short delFlag;

    /**
     * 统筹区划
     */
    private String regionCoordination;

    /**
     * 省级行政区划
     */
    private String province;

    /**
     * 地级行政区划
     */
    private String city;

    /**
     * 县级行政区划
     */
    private String county;

    /**
     * 机构数量
     */
    private Integer orgNumber;

    /**
     * 责任单位
     */
    private String responsibleOrganization;

    /**
     * 标准org主键
     */
    private Long orgId;

    /**
     * 审核状态:0-注册，1-待审核，2-启用
     */
    private Short orgState;

    /**
     * 联系人姓名
     */
    private String linkmanName;

    /**
     * 联系人手机号
     */
    private String linkmanPhone;

    /**
     * 联系人邮箱
     */
    private String linkmanEmail;

    /**
     * 机构级别（管道级别）
     */
    private Integer grade;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 维护员姓名
     */
    private String maintenanceManName;

    /**
     * 维护员电话
     */
    private String maintenanceManPhone;

    /**
     * 维护员邮箱
     */
    private String maintenanceManEmail;

    /**
     * 业务场景分类
     */
    private String orgType;

    /**
     * 机构别名（简称）
     */
    private String orgAlias;

    /**
     * 单位性质
     */
    private String orgPropertyName;

    /**
     * 业务场景分类
     */
    private String orgTypeName;

    /**
     * 特色专业
     */
    private String specialityName;

    /**
     * 机构地址行政区划
     */
    private String addrCountyCode;

    /**
     * 经济类型编码
     */
    private String economicTypeCode;

    /**
     * 经济类型名称
     */
    private String economicTypeName;

    /**
     * 法定代表人
     */
    private String legalPersonName;

    /**
     * 企业负责人
     */
    private String directorName;

    /**
     * 企业分类码
     */
    private String classNo;

    /**
     * 等级名称
     */
    private String orgLevelName;

    /**
     * 单位隶属关系代码
     */
    private String orgAffiliationCode;

    /**
     * 单位隶属关系名称
     */
    private String orgAffiliationName;

    /**
     * 组织机构
     */
    private String orgNo;

    /**
     * 是否是发生地:0-否；1-是
     */
    private String venueFlag;
}
