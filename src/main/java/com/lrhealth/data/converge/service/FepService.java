package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.model.FileInfo;

import java.util.List;

/**
 * <p>
 * 前置机处理接口
 * </p>
 *
 * @author lr
 * @since 2023/7/19 11:44
 */
public interface FepService {

    /**
     * 文件扫描，调取前置机服务
     * @param filePath
     * @return
     */
    List<FileInfo> fepFileList(String filePath);
}
