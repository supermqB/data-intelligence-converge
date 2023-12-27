package com.lrhealth.data.converge.model.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 汇聚配置
 * </p>
 *
 * @author lr
 * @since 2023-07-22
 */
@Data
@Builder
public class ConvergeConfigDto implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 主键id
     */
    private Long convergeId;

    /**
     * 配置项名称
     */
    private String convergeName;

    /**
     * 系统编码
     */
    private String sysCode;

    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 系统名称
     */
    private String sysName;

    /**
     * 机构名称
     */
    private String orgName;

}
