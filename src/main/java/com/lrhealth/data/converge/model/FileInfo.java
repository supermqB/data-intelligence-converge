package com.lrhealth.data.converge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 采集文件信息
 *
 * @author lr
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo implements Serializable {
    /**
     * 文件大小
     */
    private Long fileSize;
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件最后修改毫秒值
     */
    private Long lastModified;
}
