package com.lrhealth.data.converge.model.dto;

import lombok.Data;

/**
 * <p>
 * XDS基础信息
 * </p>
 *
 * @author lr
 * @since 2023-07-19
 */
@Data
public class TaskStateDto {
    /**
     * 主键id
     */
    private Long id;
    /**
     * 机构编码
     */
    private String orgCode;
    /**
     * 数据汇聚失败描述
     */
    private String dataConvergeDesc;
}
