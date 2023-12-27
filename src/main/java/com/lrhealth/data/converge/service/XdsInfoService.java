package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.dto.*;
import org.springframework.web.multipart.MultipartFile;

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
     * @param config  汇聚配置信息
     * @return XDS信息
     */
    Xds createXdsInfo(TaskDto taskDto, FileExecInfoDTO config);

    /**
     * 更新状态为完成
     *
     * @param taskDto 主键ID
     * @return XDS信息
     */
    Xds updateXdsCompleted(TaskDto taskDto);

    /**
     * 更新Kafka发送状态
     *
     * @param xds Xds信息
     * @return XDS信息
     */
    Xds updateKafkaSent(Xds xds);

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

    /**
     * 根据ID查询XDS信息
     *
     * @param id 主键ID
     * @return XDS信息
     */
    Xds getById(Long id);

    /**
     * 文件采集新增XDS，填充基本信息
     */
    Xds createFileXds(FileExecInfoDTO fileExecInfoDTO);

    /**
     * flinkCDC采集过程新建xds
     * @param dto flink采集信息
     * @return xds
     */
    Xds createFlinkXds(FlinkTaskDto dto, FileExecInfoDTO fileExecInfoDTO);

    Xds createDictXds(String orgCode, String sysCode, MultipartFile file);

    /**
     * 库到库的采集模式下，前置机主动生成xdsId,通知汇聚生成xds
     * @param dbXdsMessageDto
     */
    Boolean fepCreateXds(DbXdsMessageDto dbXdsMessageDto);

    /**
     * 库到库的采集模式下，前置机主动生成xdsId,通知汇聚生成xds
     * @param dbXdsMessageDto
     */
    Boolean fepUpdateXds(DbXdsMessageDto dbXdsMessageDto);
}
