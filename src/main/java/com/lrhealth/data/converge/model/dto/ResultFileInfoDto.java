package com.lrhealth.data.converge.model.dto;

import lombok.*;

/**
 * @author jinmengyu
 * @date 2023-11-07
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultFileInfoDto extends TaskInfoKafkaDto{

    private Integer resultFileId;

    private String tableName;

    private String filePath;

    private String fileName;

    /**
     * 文件状态：1-prepared/2-tranfering/3-downloaded/4-failed/5-stored
     */
    private Integer status;

    /**
     * 1-结构化，2-非结构化
     */
    private Integer fileType;

    private Long dataSize;

}
