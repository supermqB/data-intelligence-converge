package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.common.constant.CommonConstant;
import com.lrhealth.data.common.enums.conv.*;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.common.util.OdsModelUtil;
import com.lrhealth.data.converge.dao.entity.System;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.dao.service.SystemService;
import com.lrhealth.data.converge.dao.service.XdsService;
import com.lrhealth.data.converge.model.ConvFileInfoDto;
import com.lrhealth.data.converge.model.FileExecInfoDTO;
import com.lrhealth.data.converge.model.FlinkTaskDto;
import com.lrhealth.data.converge.model.TaskDto;
import com.lrhealth.data.converge.model.dto.DbXdsMessageDto;
import com.lrhealth.data.converge.service.DbSqlService;
import com.lrhealth.data.converge.service.KafkaService;
import com.lrhealth.data.converge.service.OdsModelService;
import com.lrhealth.data.converge.service.XdsInfoService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

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
    @Resource
    private OdsModelService dataTypeService;
    @Resource
    private SystemService systemService;
    @Resource
    private DbSqlService dbSqlService;
    @Resource
    private KafkaService kafkaService;


    @Override
    public Xds createXdsInfo(TaskDto taskDto, FileExecInfoDTO config) {
        Xds xds = build(taskDto, config);
        xdsService.save(xds);
        return xds;
    }

    @Override
    public Xds updateXdsCompleted(TaskDto taskDto) {
        Xds xds = getXdsInfoById(taskDto.getXdsId());
        xds.setDataConvergeEndTime(taskDto.getEndTime());
        xds.setDataType(dataTypeService.getTableDataType(xds.getOdsModelName(), xds.getSysCode()));
        if (CharSequenceUtil.isNotBlank(taskDto.getCountNumber())) {
            xds.setDataCount(Integer.valueOf(taskDto.getCountNumber()));
        }
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
        xds.setBatchNo(IdUtil.randomUUID());
        xds.setDataConvergeEndTime(LocalDateTime.now());
        xds.setDataCount(Integer.valueOf(dto.getDataCount()));
        xds.setDataType(dataTypeService.getTableDataType(xds.getOdsModelName(), xds.getSysCode()));
        return updateXdsStatus(xds, XdsStatusEnum.COMPLETED.getCode(), EMPTY);
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

    @Override
    public Xds createFileXds(FileExecInfoDTO fileExecInfoDTO) {
        Xds xds = Xds.builder()
                .id(IdUtil.getSnowflakeNextId())
                .convergeMethod(fileExecInfoDTO.getConvergeMethod())
                .delFlag(LogicDelFlagIntEnum.NONE.getCode())
                .orgCode(fileExecInfoDTO.getOrgCode())
                .sysCode(fileExecInfoDTO.getSysCode())
                .dataConvergeStartTime(LocalDateTime.now())
                .kafkaSendFlag(KafkaSendFlagEnum.NONE.getCode())
                .createTime(LocalDateTime.now())
                .createBy(CommonConstant.DEFAULT_USER)
                .build();
        xdsService.save(xds);
        return xds;
    }

    @Override
    public Xds createFlinkXds(FlinkTaskDto dto, FileExecInfoDTO config) {
        LocalDateTime convergeEndTime = Instant.ofEpochMilli(dto.getConvergeTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        Xds xds = Xds.builder()
                .id(dto.getXdsId())
                .batchNo(String.valueOf(dto.getXdsId()))
                .dataConvergeEndTime(convergeEndTime)
                .odsTableName(config.getSysCode() + "_" + dto.getTableName().toUpperCase())
                .odsModelName(dto.getTableName())
                .taskInstanceName(dto.getJobName())
                .convergeMethod(config.getConvergeMethod())
                .dataType(dataTypeService.getTableDataType(dto.getTableName(), dto.getSourceId()))
                .delFlag(LogicDelFlagIntEnum.NONE.getCode())
                .orgCode(config.getOrgCode())
                .sysCode(config.getSysCode())
                .kafkaSendFlag(KafkaSendFlagEnum.NONE.getCode())
                .createTime(LocalDateTime.now())
                .createBy(CommonConstant.DEFAULT_USER)
                .build();
        xdsService.save(xds);
        return xds;
    }

    @Override
    public Xds createDictXds(String orgCode, String sysCode, MultipartFile file) {
        Xds xds = Xds.builder()
                .id(IdUtil.getSnowflakeNextId())
                .orgCode(orgCode).sysCode(sysCode)
                .convergeMethod(ConvMethodEnum.FILE.getCode()).dataType(CollectDataTypeEnum.DICT.getCode())
                .dataConvergeStartTime(LocalDateTime.now()).dataConvergeStatus(XdsStatusEnum.INIT.getCode())
                .oriFileFromIp(NetUtil.getLocalhostStr()).odsModelName("dict")
                .oriFileName(file.getOriginalFilename()).oriFileType("xlsx")
                .oriFileSize(BigDecimal.valueOf(file.getSize()))
                .odsTableName(sysCode + "_" + "dict")
                .createTime(LocalDateTime.now()).delFlag(0)
                .kafkaSendFlag(0)
                .build();
        xdsService.save(xds);
        return xdsService.getById(xds.getId());
    }

    @Override
    public void fepCreateXds(DbXdsMessageDto dbXdsMessageDto) {
        String dataType = dataTypeService.getTableDataType(dbXdsMessageDto.getOdsModelName(), dbXdsMessageDto.getSysCode());
        if (CharSequenceUtil.isBlank(dataType)) {
            dataType = CollectDataTypeEnum.BUSINESS.getCode();
        }
        List<System> systemList = systemService.list(new LambdaQueryWrapper<System>().eq(System::getSystemCode, dbXdsMessageDto.getSysCode())
                .eq(System::getDelFlag, 0));
        if (CollUtil.isEmpty(systemList)) {
            return;
        }
        Xds xds = Xds.builder()
                .id(dbXdsMessageDto.getId())
                .orgCode(systemList.get(0).getSourceCode())
                .sysCode(dbXdsMessageDto.getSysCode())
                .convergeMethod(dbXdsMessageDto.getConvergeMethod())
                .dataType(dataType)
                .dataConvergeStartTime(LocalDateTime.now())
                .dataConvergeStatus(XdsStatusEnum.INIT.getCode())
                .odsModelName(dbXdsMessageDto.getOdsModelName())
                .odsTableName(dbXdsMessageDto.getOdsTableName())
                .createTime(LocalDateTime.now())
                .build();
        xdsService.save(xds);
    }

    @Override
    public void fepUpdateXds(DbXdsMessageDto dbXdsMessageDto) {
        int dataCount = dbXdsMessageDto.getDataCount() == null ? 0 : dbXdsMessageDto.getDataCount();
        Integer avgRowLength = dbSqlService.getAvgRowLength(dbXdsMessageDto.getOdsTableName(), dbXdsMessageDto.getOdsModelName());
        // 文件中的数据写入后消耗的数据库容量
        long dataSize = dataCount * avgRowLength;
        Xds updateXds = Xds.builder()
                .id(dbXdsMessageDto.getId())
                .dataConvergeEndTime(LocalDateTime.now())
                .dataConvergeStatus(XdsStatusEnum.COMPLETED.getCode())
                .dataCount(dataCount)
                .dataSize(dataSize)
                .updateTime(LocalDateTime.now())
                .build();
        xdsService.updateById(updateXds);

        // 发送kafka
        kafkaService.xdsSendKafka(updateXds);
    }

    /**
     * 组装XDS信息
     *
     * @param taskDto 请求参数
     * @param config  配置信息
     * @return XDS信息
     */
    private Xds build(TaskDto taskDto, FileExecInfoDTO config) {
        Xds xds = Xds.builder()
                .id(IdUtil.getSnowflakeNextId())
                .convergeMethod(config.getConvergeMethod())
                .delFlag(LogicDelFlagIntEnum.NONE.getCode())
                .dataCount(isNotBlank(taskDto.getCountNumber()) ? Integer.parseInt(taskDto.getCountNumber()) : 0)
                .orgCode(config.getOrgCode())
                .sysCode(config.getSysCode())
                .dataConvergeStartTime(taskDto.getStartTime())
                .dataConvergeStatus(XdsStatusEnum.INIT.getCode())
                .batchNo(taskDto.getBatchNo())
                .kafkaSendFlag(KafkaSendFlagEnum.NONE.getCode())
                .createTime(LocalDateTime.now())
                .createBy(CommonConstant.DEFAULT_USER)
                .build();
        // 整合文件不知道表名的情况
        if (isNotBlank(taskDto.getOdsTableName())) {
            xds.setOdsTableName(taskDto.getOdsTableName());
            xds.setOdsModelName(OdsModelUtil.getModelName(config.getSysCode(), taskDto.getOdsTableName()));
        }
        return xds;
    }

    /**
     * 设置文件信息参数
     *
     * @param xds XDS信息
     * @param dto 文件信息
     * @return XDS信息
     */
    private Xds setFileInfo(Xds xds, ConvFileInfoDto dto) {
        xds.setStoredFileMode(dto.getStoredFileMode() == null ? XdsStoredFileModeEnum.LOCAL.getCode() : dto.getStoredFileMode());
        xds.setStoredFilePath(dto.getStoredFilePath());
        xds.setStoredFileName(dto.getStoredFileName());
        xds.setStoredFileType(dto.getStoredFileType());
        xds.setStoredFileSize(dto.getStoredFileSize());
        xds.setOriFileFromIp(dto.getOriFileFromIp());
        xds.setOriFileType(dto.getOriFileType());
        xds.setOriFileName(dto.getOriFileName());
        xds.setOriFileSize(dto.getOriFileSize());
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
    private Xds updateXdsStatus(Xds xds, Integer status, String errorMsg) {
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
