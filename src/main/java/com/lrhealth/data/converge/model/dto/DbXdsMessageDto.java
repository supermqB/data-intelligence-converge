package com.lrhealth.data.converge.model.dto;

import lombok.Data;

/**
 * @author jinmengyu
 * @date 2023-11-21
 */
@Data
public class DbXdsMessageDto {

    private Long id;

    private String sysCode;

    private String convergeMethod;

    private String odsModelName;

    private String odsTableName;

    private Integer dataCount;

    private Long tunnelId;

    private Integer dsConfigId;

    private Integer convTaskId;

}
