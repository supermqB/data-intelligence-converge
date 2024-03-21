package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2024-03-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileCollectInfoDto {

    /**
     * 文件地址
     */
    private String fileModeCollectDir;

    /**
     * 文件采集范围
     */
    private String collectRange;

    /**
     * 结构化数据标识
     * 1-结构化数据，2-非结构化数据
     */
    private Integer structuredDataFlag;
}
