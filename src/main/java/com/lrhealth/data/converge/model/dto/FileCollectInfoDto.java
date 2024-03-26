package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
     * 结构化数据
     * 文件采集范围
     */
    private String fileCollectRange;

    /**
     * 文件入库方式
     * 1-数据库 2-dicom 3-对象存储
     */
    private Integer fileStorageMode;

    /**
     * 文件后缀列表
     */
    private List<String> fileSuffix;

    /**
     * 结构化数据标识
     * 1-结构化数据，2-非结构化数据
     */
    private Integer structuredDataFlag;
}
