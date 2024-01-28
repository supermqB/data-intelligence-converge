package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.enums.LibraryTableModelEnum;
import com.lrhealth.data.converge.common.enums.SeqFieldTypeEnum;
import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import com.lrhealth.data.converge.dao.entity.*;
import com.lrhealth.data.converge.dao.service.*;
import com.lrhealth.data.converge.model.dto.*;
import com.lrhealth.data.converge.scheduled.DownloadFileTask;
import com.lrhealth.data.converge.service.ConvergeService;
import com.lrhealth.data.converge.service.FeTunnelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.System;
import java.util.*;

/**
 * @author jinmengyu
 * @date 2023-11-14
 */
@Slf4j
@Service
public class FeTunnelConfigServiceImpl implements FeTunnelConfigService {

    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private ConvFeNodeService feNodeService;
    @Resource
    private ConvCollectFieldService collectFieldService;
    @Resource
    private ConvergeService convergeService;
    @Resource
    private ConvOdsDatasourceConfigService odsDatasourceConfigService;
    @Resource
    private ConvCollectIncrTimeService incrTimeService;


    @Override
    public List<TunnelMessageDTO> getFepTunnelConfig(String ip, Integer port) {
        List<TunnelMessageDTO> messageDTOList = new ArrayList<>();
        if (ip.equals(System.getProperty("converge.ip"))){
            directTunnelConfig(messageDTOList);
            return messageDTOList;
        }
        // 查询到所有创建的前置机
        List<ConvFeNode> fepList = getFepListByIpAndPort(ip, port);
        if (CollUtil.isEmpty(fepList)) return CollUtil.newArrayList();
        // 组装管道信息
        fepList.forEach(fep -> feNodeTunnelConfig(messageDTOList, fep));
        return messageDTOList;
    }

    @Override
    @Async
    public void updateFepStatus(ActiveFepUploadDto activeFepUploadDto) {
        FrontendStatusDto frontendStatusDto = activeFepUploadDto.getFrontendStatusDto();
        List<ConvFeNode> fepList = getFepListByIpAndPort(activeFepUploadDto.getIp(), activeFepUploadDto.getPort());
        if (fepList.isEmpty()) return;
        if (frontendStatusDto == null || frontendStatusDto.getTunnelStatusDtoList() == null) {
            log.error("fep status上报日志异常: " + frontendStatusDto);
            return;
        }
        convergeService.updateFepStatus(frontendStatusDto, DownloadFileTask.taskDeque);
    }

    private List<ConvFeNode> getFepListByIpAndPort(String ip, Integer port){
        if (CharSequenceUtil.isBlank(ip)){
            throw new CommonException("请输入前置机ip地址");
        }
        List<ConvFeNode> fepList = feNodeService.list(new LambdaQueryWrapper<ConvFeNode>().eq(ConvFeNode::getIp, ip)
                .eq((port != null), ConvFeNode::getPort, port)
                .eq(ConvFeNode::getDelFlag, 0));
        if (CollUtil.isEmpty(fepList)){
            return Collections.emptyList();
        }
        return fepList;
    }

    /**
     * 直连管道配置
     * @param messageDTOList
     */
    public void directTunnelConfig(List<TunnelMessageDTO> messageDTOList){
        List<ConvTunnel> tunnelList = tunnelService.list(new LambdaQueryWrapper<ConvTunnel>().eq(ConvTunnel::getFrontendId, -1));
        if (CollUtil.isEmpty(tunnelList)){
            return;
        }
        tunnelList.forEach(tunnel -> {
            TunnelMessageDTO tunnelMessageDTO = getTunnelMessage(tunnel);
            messageDTOList.add(tunnelMessageDTO);
        });
    }

    private void feNodeTunnelConfig(List<TunnelMessageDTO> messageDTOList, ConvFeNode fep){
        List<ConvTunnel> tunnelList = tunnelService.list(new LambdaQueryWrapper<ConvTunnel>().eq(ConvTunnel::getFrontendId, fep.getId()));
        if (CollUtil.isEmpty(tunnelList)){
            return;
        }
        tunnelList.forEach(tunnel -> {
            TunnelMessageDTO tunnelMessageDTO = getTunnelMessage(tunnel);
            messageDTOList.add(tunnelMessageDTO);
        });
    }

    @Override
    public TunnelMessageDTO getTunnelMessage(ConvTunnel tunnel){
        TunnelMessageDTO tunnelMessageDTO = new TunnelMessageDTO();
        // 管道基本信息
        BeanUtil.copyProperties(tunnel, tunnelMessageDTO);
        tunnelMessageDTO.setTimeDif(getCronTimeUnit(tunnel.getTimeDif(), tunnel.getTimeUnit()));
        if (tunnel.getConvergeMethod().equals(TunnelCMEnum.LIBRARY_TABLE.getCode())
                || tunnel.getConvergeMethod().equals(TunnelCMEnum.CDC_LOG.getCode())){
            // 库表/日志的读库信息
            JdbcInfoDto jdbcInfoDto = new JdbcInfoDto();
            ConvOdsDatasourceConfig readerDs = odsDatasourceConfigService.getById(tunnel.getReaderDatasourceId());
            jdbcInfoDto.setJdbcUrl(CharSequenceUtil.isBlank(readerDs.getDsUrlForFront()) ? readerDs.getDsUrl() : readerDs.getDsUrlForFront());
            jdbcInfoDto.setDbUserName(readerDs.getDsUsername());
            jdbcInfoDto.setDbPasswd(readerDs.getDsPwd());
            // 库表采集
            if (tunnel.getConvergeMethod().equals(TunnelCMEnum.LIBRARY_TABLE.getCode())){
                // 全量/增量采集
                jdbcInfoDto.setColType(tunnel.getColType());
                jdbcInfoDto.setFullColStartTime(String.valueOf(tunnel.getFullColStartTime()));
                jdbcInfoDto.setFullColEndTime(String.valueOf(tunnel.getFullColEndTime()));
                // 库到库/库到文件
                jdbcInfoDto.setCollectModel(tunnel.getCollectModel());
                // 单表采集还是自定义sql
                jdbcInfoDto.setColTableType(tunnel.getColTableType());
                // 库到库
                if(LibraryTableModelEnum.DATABASE_TO_DATABASE.getCode().equals(jdbcInfoDto.getCollectModel())){
                    ConvOdsDatasourceConfig writerDs = odsDatasourceConfigService.getById(tunnel.getWriterDatasourceId());
                    jdbcInfoDto.setJdbcUrlForIn(writerDs.getDsUrl());
                    jdbcInfoDto.setDbUserNameForIn(writerDs.getDsUsername());
                    jdbcInfoDto.setDbPasswdForIn(writerDs.getDsPwd());
                    jdbcInfoDto.setDsConfigId(tunnel.getWriterDatasourceId());
                }
                // 表以及对应的sql信息
                assembleTableInfoMessage(tunnel, jdbcInfoDto);
            }
            tunnelMessageDTO.setJdbcInfoDto(jdbcInfoDto);
        }
        return tunnelMessageDTO;
    }


    private void assembleTableInfoMessage(ConvTunnel tunnel, JdbcInfoDto jdbcInfoDto){
        // 库表采集范围和sql查询语句
        List<TableInfoDto> tableInfoDtoList = new ArrayList<>();
        List<String> tableList = Arrays.asList(tunnel.getCollectRange().split(","));

        // 增量字段
        List<ConvCollectField> collectFieldList = collectFieldService.list(new LambdaQueryWrapper<ConvCollectField>()
                        .in(ConvCollectField::getTableName, tableList)
                        .eq(ConvCollectField::getTunnelId, tunnel.getId())
                        .eq(ConvCollectField::getSystemCode, tunnel.getSysCode()));

        collectFieldList.forEach(model -> {
            TableInfoDto tableInfoDto = new TableInfoDto();
            tableInfoDto.setTableName(model.getTableName());
            tableInfoDto.setSqlQuery(model.getQuerySql());
            tableInfoDto.setWriterColumns(model.getColumnField());
            String incrField = model.getConditionField();
            if (CharSequenceUtil.isNotBlank(incrField)){
                // 1-时间 2-序列
                String fieldType = model.getConditionFieldType();
                tableInfoDto.setSeqField(incrField);
                tableInfoDto.setSeqFieldType(model.getConditionFieldType());
                Map<String, String> fieldMap = new HashMap<>();
                getIncrFieldMap(tunnel, model.getConditionField(), model.getTableName(), fieldType, fieldMap);
                tableInfoDto.setIncrTimeMap(fieldMap);
            }
            tableInfoDtoList.add(tableInfoDto);
        });
        jdbcInfoDto.setTableInfoDtoList(tableInfoDtoList);
    }

    private void getIncrFieldMap(ConvTunnel tunnel, String column, String tableName, String fieldType, Map<String, String> fieldMap){
        ConvCollectIncrTime collectIncrTime = incrTimeService.getOne(new LambdaQueryWrapper<ConvCollectIncrTime>()
                .eq(ConvCollectIncrTime::getTunnelId, tunnel.getId())
                .eq(ConvCollectIncrTime::getIncrField, column)
                .eq(ConvCollectIncrTime::getTableName, tableName));
        if (ObjectUtil.isNotNull(collectIncrTime)) {
            if (SeqFieldTypeEnum.TIME.getValue().equals(fieldType)){
                fieldMap.put(column, collectIncrTime.getLatestTime());
            }else {
                fieldMap.put(column, collectIncrTime.getLatestSeq());
            }
        }
    }

    private Integer getCronTimeUnit(Integer timeDif, String timeUnit){
        int seconds = 0;
        // 转换时间为毫秒
        if (CharSequenceUtil.isBlank(timeUnit)){
            return seconds;
        }
        //小时
        if (timeUnit.equals("h")){
            seconds = timeDif * 3600;
        }
        //分钟
        if (timeUnit.equals("m")){
            seconds = timeDif * 60;
        }
        return seconds * 1000;

    }
}
