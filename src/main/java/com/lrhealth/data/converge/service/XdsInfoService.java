package com.lrhealth.data.converge.service;

import com.baomidou.mybatisplus.extension.service.IService;
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
public interface XdsInfoService  extends IService<Xds> {
    /**
     * 更新Kafka发送状态
     *
     * @param xds Xds信息
     * @return XDS信息
     */
    Xds updateKafkaSent(Xds xds);

    /**
     * 根据ID查询XDS信息
     *
     * @param id 主键ID
     * @return XDS信息
     */
    Xds getById(Long id);

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
