package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.common.constants.CommonConstant;
import com.lrhealth.data.converge.common.enums.CollectDataTypeEnum;
import com.lrhealth.data.converge.common.enums.ConvMethodEnum;
import com.lrhealth.data.converge.common.enums.KafkaSendFlagEnum;
import com.lrhealth.data.converge.common.enums.XdsStatusEnum;
import com.lrhealth.data.converge.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.ConvTask;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.entity.System;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.dao.mapper.XdsMapper;
import com.lrhealth.data.converge.dao.service.ConvTaskService;
import com.lrhealth.data.converge.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.dao.service.SystemService;
import com.lrhealth.data.converge.dao.service.XdsService;
import com.lrhealth.data.converge.model.dto.DataSourceDto;
import com.lrhealth.data.converge.model.dto.DbXdsMessageDto;
import com.lrhealth.data.converge.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static cn.hutool.core.text.CharSequenceUtil.format;

/**
 * <p>
 * XDS信息实现类
 * </p>
 *
 * @author lr
 * @since 2023/7/19 11:44
 */
@Service
public class XdsInfoServiceImpl  extends ServiceImpl<XdsMapper, Xds> implements XdsInfoService {
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
    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private ConvTaskService taskService;
    @Resource
    private IncrTimeService incrTimeService;
    @Value("${xds.switch-on}")
    private boolean xdsSendToGove;


    @Override
    public Xds updateKafkaSent(Xds xds) {
        xds.setKafkaSendFlag(KafkaSendFlagEnum.SENT.getCode());
        return updateXds(xds);
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
    public Boolean fepCreateXds(DbXdsMessageDto dbXdsMessageDto) {
        String dataType = dataTypeService.getTableDataType(dbXdsMessageDto.getOdsModelName(), dbXdsMessageDto.getSysCode());
        if (CharSequenceUtil.isBlank(dataType)) {
            dataType = CollectDataTypeEnum.BUSINESS.getCode();
        }
        List<System> systemList = systemService.list(new LambdaQueryWrapper<System>().eq(System::getSystemCode, dbXdsMessageDto.getSysCode())
                .eq(System::getDelFlag, 0));
        if (CollUtil.isEmpty(systemList)) {
            return false;
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
                .dsConfigId(dbXdsMessageDto.getDsConfigId())
                .xdsId(dbXdsMessageDto.getXdsId())
                .build();
        return xdsService.save(xds);
    }

    @Override
    public Boolean fepUpdateXds(DbXdsMessageDto dbXdsMessageDto) {
        long dataCount = dbXdsMessageDto.getDataCount() == null ? 0 : dbXdsMessageDto.getDataCount();
        DataSourceDto dataSourceDto = tunnelService.getWriterDataSourceByTunnel(dbXdsMessageDto.getTunnelId());
        long avgRowLength = dbSqlService.getAvgRowLength(dbXdsMessageDto.getOdsTableName(), dataSourceDto, dbXdsMessageDto.getOdsModelName());
        // 文件中的数据写入后消耗的数据库容量
        long dataSize = avgRowLength * dataCount;
        List<ConvTask> convTasks = taskService.list(new LambdaQueryWrapper<ConvTask>()
                .eq(ConvTask::getTunnelId, dbXdsMessageDto.getTunnelId())
                .eq(ConvTask::getFedTaskId, dbXdsMessageDto.getConvTaskId())
                .orderByDesc(ConvTask::getCreateTime));
        ConvTunnel tunnel = tunnelService.getById(dbXdsMessageDto.getTunnelId());
        if (CollUtil.isEmpty(convTasks) || ObjectUtil.isNull(tunnel)){
            return false;
        }
        Xds updateXds = Xds.builder()
                .id(dbXdsMessageDto.getId())
                .dataConvergeEndTime(LocalDateTime.now())
                .dataConvergeStatus(XdsStatusEnum.COMPLETED.getCode())
                .dataCount(dataCount)
                .dataSize(dataSize)
                .updateTime(LocalDateTime.now())
                .convTaskId(convTasks.get(0).getId())
                .colType(tunnel.getColType())
                .build();
        xdsService.updateById(updateXds);

        // 发送kafka
        if (xdsSendToGove) {
            kafkaService.xdsSendKafka(updateXds);
        }
        // 更新最新采集时间
        incrTimeService.updateTableLatestTime(dbXdsMessageDto.getId(), dbXdsMessageDto.getEndIndex());
        return true;
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
