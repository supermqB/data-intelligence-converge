package com.lrhealth.data.converge.dao.entity;

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
     * 主键ID
     */
    private Long id;

    /**
     * 前置机编码
     */
    private String frontendId;

    /**
     * 前置机名称
     */
    private String frontendName;

    /**
     * 前置机ip地址
     */
    private String fileFromIp;

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
     */
    private Integer deleted;

    private String serverIp;
}
