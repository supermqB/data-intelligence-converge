package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 机构系统关系表
 * </p>
 *
 * @author jinmengyu
 * @since 2023-07-20
 */
@Data
@TableName("conv_institution_system_relation")
public class InstitutionSystemRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 机构ID
     */
    private Long sourceId;

    /**
     * 系统ID
     */
    private Long systemId;

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
     * 逻辑删除字段，0-表示有效，1-表示删除
     */
    private Short delFlag;
}
