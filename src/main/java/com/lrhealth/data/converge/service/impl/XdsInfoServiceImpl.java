package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.lrhealth.data.common.enums.conv.XdsStatusEnum;
import com.lrhealth.data.common.enums.conv.XdsStoredFileModeEnum;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.dao.service.XdsService;
import com.lrhealth.data.converge.model.ConvFileInfoDto;
import com.lrhealth.data.converge.model.TaskDto;
import com.lrhealth.data.converge.service.XdsInfoService;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;

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
    private static final String DEFAULT_USER = "sys";

    @Value("${spring.kafka.topic.xds}")
    private String topic;
    @Resource
    private XdsService xdsService;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public Xds createXdsInfo(TaskDto taskDto) {
        Xds xds = BeanUtil.copyProperties(taskDto, Xds.class);
        xds.setCreateTime(LocalDateTime.now());
        xds.setCreateBy(DEFAULT_USER);
        xds.setDataConvergeStartTime(taskDto.getStartTime());
        xdsService.save(xds);
        return xds;
    }

    @Override
    public Xds updateXdsCompleted(Long id) {
        Xds xds = getXdsInfoById(id);
        return updateXdsStatus(xds, XdsStatusEnum.COMPLETED.getCode(), EMPTY);
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
        xds.setUpdateBy(DEFAULT_USER);
        xds.setDataConvergeEndTime(LocalDateTime.now());
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


    private void xdsSendKafka(Xds xds) {
        Xds checkXds = xdsService.getById(xds.getId());
        if (ObjectUtil.isNotNull(checkXds) && (!NumberUtils.INTEGER_ONE.equals(xds.getKafkaSendFlag()))) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", checkXds.getId());
            kafkaTemplate.send(topic, JSON.toJSONString(map));
            xds.setKafkaSendFlag(1);
            xdsService.updateById(xds);
        }
    }
}
