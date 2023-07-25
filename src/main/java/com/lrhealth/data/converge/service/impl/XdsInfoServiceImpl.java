package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.lrhealth.data.common.constant.CommonConstant;
import com.lrhealth.data.common.enums.conv.KafkaSendFlagEnum;
import com.lrhealth.data.common.enums.conv.LogicDelFlagIntEnum;
import com.lrhealth.data.common.enums.conv.XdsStatusEnum;
import com.lrhealth.data.common.enums.conv.XdsStoredFileModeEnum;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.common.util.OdsModelUtil;
import com.lrhealth.data.converge.dao.entity.ConvergeConfig;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.dao.service.XdsService;
import com.lrhealth.data.converge.model.ConvFileInfoDto;
import com.lrhealth.data.converge.model.TaskDto;
import com.lrhealth.data.converge.service.XdsInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.hutool.core.text.CharSequenceUtil.*;

/**
 * <p>
 * XDS信息实现类
 * </p>
 *
 * @author lr
 * @since 2023/7/19 11:44
 */
@Service
public class XdsInfoServiceImpl implements XdsInfoService {
    @Resource
    private XdsService xdsService;


    @Override
    public Xds createXdsInfo(TaskDto taskDto, ConvergeConfig config) {
        Xds xds = build(taskDto, config);
        xdsService.save(xds);
        return xds;
    }

    @Override
    public Xds updateXdsCompleted(TaskDto taskDto) {
        Xds xds = getXdsInfoById(taskDto.getXdsId());
        xds.setBatchNo(taskDto.getBatchNo());
        xds.setDataConvergeEndTime(taskDto.getEndTime());
        return updateXdsStatus(xds, XdsStatusEnum.COMPLETED.getCode(), EMPTY);
    }

    @Override
    public Xds updateKafkaSent(Xds xds) {
        xds.setKafkaSendFlag(KafkaSendFlagEnum.SENT.getCode());
        return updateXds(xds);
    }

    @Override
    public Xds updateXdsFailure(Long id, String errorMsg) {
        Xds xds = getXdsInfoById(id);
        return updateXdsStatus(xds, XdsStatusEnum.FAILURE.getCode(), errorMsg);
    }

    @Override
    public Xds updateXdsFileInfo(ConvFileInfoDto dto) {
        Xds xds = getXdsInfoById(dto.getId());
        return updateXds(setFileInfo(xds, dto));
    }

    @Override
    public Xds updateXdsCompleted(ConvFileInfoDto dto) {
        Xds xds = getXdsInfoById(dto.getId());
        return updateXdsStatus(setFileInfo(xds, dto), XdsStatusEnum.COMPLETED.getCode(), EMPTY);
    }

    @Override
    public Xds getById(Long id) {
        if (null == id) {
            throw new CommonException("参数【XDS信息主键ID】为空");
        }
        Xds xds = xdsService.getById(id);
        if (null == xds) {
            throw new CommonException(format("XDsID={}对应的XDS信息不存在", id));
        }
        return xds;
    }

    /**
     * 组装XDS信息
     *
     * @param taskDto 请求参数
     * @param config  配置信息
     * @return XDS信息
     */
    private Xds build(TaskDto taskDto, ConvergeConfig config) {
        return Xds.builder()
                .convergeMethod(config.getConvergeMethod())
                .batchNo(taskDto.getBatchNo())
                .dataType(config.getDataType())
                .delFlag(LogicDelFlagIntEnum.NONE.getCode())
//                .hpsCode(taskDto.getHpsCode())
//                .dataConvergeStatus()
//                .dataConvergeDesc()
                .dataCount(isNotBlank(taskDto.getCountNumber()) ? Integer.parseInt(taskDto.getCountNumber()) : 0)
                .dataConvergeEndTime(taskDto.getEndTime())
                .orgCode(config.getOrgCode())
                .dataConvergeStartTime(taskDto.getStartTime())
                .odsTableName(taskDto.getOdsTableName())
                .kafkaSendFlag(KafkaSendFlagEnum.NONE.getCode())
                .odsModelName(OdsModelUtil.getModelName(config.getSysCode(), taskDto.getOdsTableName()))
                .createTime(LocalDateTime.now())
                .createBy(CommonConstant.DEFAULT_USER)
                .build();
    }

    /**
     * 设置文件信息参数
     *
     * @param xds XDS信息
     * @param dto 文件信息
     * @return XDS信息
     */
    private Xds setFileInfo(Xds xds, ConvFileInfoDto dto) {
        xds.setOriFileFromIp(dto.getOriFileFromIp());
        xds.setOriFileType(dto.getOriFileType());
        xds.setOriFileSize(dto.getOriFileSize());
        xds.setOriFileName(dto.getOriFileName());
        xds.setStoredFileMode(dto.getStoredFileMode() == null ? XdsStoredFileModeEnum.LOCAL.getCode() : dto.getStoredFileMode());
        xds.setStoredFilePath(dto.getStoredFilePath());
        xds.setStoredFileName(dto.getStoredFileName());
        xds.setStoredFileType(dto.getStoredFileType());
        xds.setStoredFileSize(dto.getStoredFileSize());
        return xds;
    }

    /**
     * 更新状态信息
     *
     * @param xds      Xds信息
     * @param status   状态 @see status
     * @param errorMsg 异常信息
     * @return xds信息
     * @see XdsStatusEnum
     */
    public Xds updateXdsStatus(Xds xds, Integer status, String errorMsg) {
        xds.setDataConvergeStatus(status);
        xds.setDataConvergeDesc(errorMsg);
        return updateXds(xds);
    }

    /**
     * 更新状态信息
     *
     * @param xds Xds信息
     * @return xds信息
     * @see XdsStatusEnum
     */
    public Xds updateXds(Xds xds) {
        xds.setUpdateTime(LocalDateTime.now());
        xds.setUpdateBy(CommonConstant.DEFAULT_USER);
//        xds.setDataConvergeEndTime(LocalDateTime.now());
        xdsService.updateById(xds);
        return getXdsInfoById(xds.getId());
    }

    /**
     * 根据主键ID查询XDS信息
     *
     * @param id 主键ID
     * @return XDS信息
     */
    private Xds getXdsInfoById(Long id) {
        Xds xds = xdsService.getById(id);
        if (BeanUtil.isEmpty(xds)) {
            throw new CommonException("任务信息不存在");
        }
        return xds;
    }


}
