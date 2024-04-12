package com.lrhealth.data.converge.model.dto;

import lombok.*;

/**
 * @author jinmengyu
 * @date 2023-09-20
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultViewInfoDto extends TaskInfoKafkaDto{

    private Integer resultViewId;

    private String filePath;

    private String fileName;

    private String tableName;

    private String startIndex;

    private String endIndex;

    private Long recordCount;

    private Integer status;

    private Long fileSize;

    private String storedTime;
}
