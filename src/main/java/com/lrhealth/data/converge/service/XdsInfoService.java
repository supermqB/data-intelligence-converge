package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.ConvFileInfoDto;
import com.lrhealth.data.converge.model.TaskDto;

/**
 * <p>
 * XDS接口
 * </p>
 *
 * @author lr
 * @since 2023/7/19 11:44
 */
public interface XdsInfoService {

    /**
     * 创建XDS信息
     *
     * @param taskDto 任务信息
     * @return XDS信息
     */
    Xds createXdsInfo(TaskDto taskDto);

    /**
     * 更新状态为完成
     *
     * @param id 主键ID
     * @return XDS信息
     */
    Xds updateXdsCompleted(Long id);

    /**
     * 更新状态为完成
     *
     * @param fileInfoDto 文件信息
     * @return XDS信息
     */
    Xds updateXdsCompleted(ConvFileInfoDto fileInfoDto);

    /**
     * 更新状态为失败
     *
     * @param id       主键ID
     * @param errorMsg 异常信息
     * @return XDS信息
     */
    Xds updateXdsFailure(Long id, String errorMsg);

    /**
     * 更新文件信息
     *
     * @param fileInfoDto 文件信息
     * @return XDS信息
     */
    Xds updateXdsFileInfo(ConvFileInfoDto fileInfoDto);
}
