package com.lrhealth.data.converge.scheduled.model.dto;

import lombok.Data;

/**
 * @author jinmengyu
 * @date 2023-09-20
 */
@Data
public class ResultViewInfoDto {

    private Integer resultViewId;

    private String filePath;

    private String fileName;

    private String tableName;

    private Integer startIndex;

    private Integer endIndex;

    private Integer recordCount;

    private Integer status;

    private Integer fileSize;
}
