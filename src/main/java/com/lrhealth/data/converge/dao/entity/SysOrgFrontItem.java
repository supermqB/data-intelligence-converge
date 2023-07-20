package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 前置机系统机构关系表
 * </p>
 *
 * @author jinmengyu
 * @since 2023-07-20
 */
@Data
@TableName("conv_sys_org_front_item")
public class SysOrgFrontItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 系统ID
     */
    private String sourceId;

    /**
     * 机构ID
     */
    private String orgId;

    /**
     * 前置机ID
     */
    private String frontendId;

    /**
     * 上下线状态
     */
    private String state;

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
}
