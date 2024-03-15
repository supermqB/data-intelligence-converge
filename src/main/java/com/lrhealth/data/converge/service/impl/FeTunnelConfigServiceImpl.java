package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.enums.LibraryTableModelEnum;
import com.lrhealth.data.converge.common.enums.SeqFieldTypeEnum;
import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import com.lrhealth.data.converge.common.enums.TunnelColTypeEnum;
import com.lrhealth.data.converge.common.util.StringUtils;
import com.lrhealth.data.converge.dao.entity.*;
import com.lrhealth.data.converge.dao.service.*;
import com.lrhealth.data.converge.model.dto.*;
import com.lrhealth.data.converge.scheduled.DownloadFileTask;
import com.lrhealth.data.converge.service.*;
import com.lrhealth.data.model.original.model.OriginalModel;
import com.lrhealth.data.model.original.model.OriginalModelColumn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.System;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private ConvFeNodeService convFeNodeService;
    @Resource
    private ConvCollectFieldService collectFieldService;
    @Resource
    private ConvergeService convergeService;
    @Resource
    private ConvOdsDatasourceConfigService odsDatasourceConfigService;
    @Resource
    private ConvCollectIncrTimeService incrTimeService;
    @Resource
    private FeNodeService feNodeService;
    @Resource
    private ConvTaskService taskService;
    @Resource
    private OdsModelService odsModelService;
    @Resource
    private ModelConfigService modelConfigService;


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

    @Override
    public void kafkaUpdateFepStatus(String key, String msgBody) {
        if (CharSequenceUtil.isBlank(key)) return;
        log.info("kafkaUpdateFepStatus,key={}",key);
        switch (key){
            case "tunnel":
                TunnelStatusKafkaDto dto = JSON.parseObject(msgBody, TunnelStatusKafkaDto.class);
                feNodeService.updateTunnel(dto);
                break;
            case "task":
                TaskInfoKafkaDto taskInfoKafkaDto = JSON.parseObject(msgBody, TaskInfoKafkaDto.class);
                ConvTask oldTask = taskService.getOne(new LambdaQueryWrapper<ConvTask>()
                        .eq(ConvTask::getFedTaskId, taskInfoKafkaDto.getTaskId())
                        .eq(ConvTask::getTunnelId, taskInfoKafkaDto.getTunnelId()), false);
                ConvTunnel tunnel = tunnelService.getById(taskInfoKafkaDto.getTunnelId());
                //更新 task
                ConvTask convTask = feNodeService.saveOrUpdateTask(taskInfoKafkaDto, tunnel, oldTask);
                convergeService.sendDsKafka(convTask, oldTask, tunnel.getId());
                break;
            case "taskResultView":
                ResultViewInfoDto resultViewInfoDto = JSON.parseObject(msgBody, ResultViewInfoDto.class);
                ConvTask task = taskService.getOne(new LambdaQueryWrapper<ConvTask>()
                        .eq(ConvTask::getFedTaskId, resultViewInfoDto.getTaskId())
                        .eq(ConvTask::getTunnelId, resultViewInfoDto.getTunnelId()));
                feNodeService.updateTaskResultView(DownloadFileTask.taskDeque, Lists.newArrayList(resultViewInfoDto), task);
                break;
            case "taskLog":
                TaskLogDto taskLogDto = JSON.parseObject(msgBody, TaskLogDto.class);
                ConvTask relateTask = taskService.getOne(new LambdaQueryWrapper<ConvTask>()
                        .eq(ConvTask::getFedTaskId, taskLogDto.getTaskId())
                        .eq(ConvTask::getTunnelId, taskLogDto.getTunnelId()));
                feNodeService.saveOrUpdateLog(Lists.newArrayList(taskLogDto), relateTask.getId());
                break;
            default:
                throw new CommonException("不存在的kafka标识：" + key);
        }
    }


    private List<ConvFeNode> getFepListByIpAndPort(String ip, Integer port){
        if (CharSequenceUtil.isBlank(ip)){
            throw new CommonException("请输入前置机ip地址");
        }
        List<ConvFeNode> fepList = convFeNodeService.list(new LambdaQueryWrapper<ConvFeNode>().eq(ConvFeNode::getIp, ip)
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
        tunnelMessageDTO.setDependenceTunnelId(CharSequenceUtil.isNotBlank(tunnel.getDependenceTunnelId()) ? Long.valueOf(tunnel.getDependenceTunnelId()) : null);
        if (tunnel.getConvergeMethod().equals(TunnelCMEnum.LIBRARY_TABLE.getCode())
                || tunnel.getConvergeMethod().equals(TunnelCMEnum.CDC_LOG.getCode())){
            // 库表/日志的读库信息
            JdbcInfoDto jdbcInfoDto = new JdbcInfoDto();
            ConvOdsDatasourceConfig readerDs = odsDatasourceConfigService.getById(tunnel.getReaderDatasourceId());
            jdbcInfoDto.setDsId(readerDs.getId());
            jdbcInfoDto.setJdbcUrl(CharSequenceUtil.isBlank(readerDs.getDsUrlForFront()) ? readerDs.getDsUrl() : readerDs.getDsUrlForFront());
            jdbcInfoDto.setDbUserName(readerDs.getDsUsername());
            jdbcInfoDto.setDbPasswd(readerDs.getDsPwd());
            jdbcInfoDto.setDbSchema(readerDs.getSchema());
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
                String dbType = null;
                if(LibraryTableModelEnum.DATABASE_TO_DATABASE.getCode().equals(jdbcInfoDto.getCollectModel())){
                    ConvOdsDatasourceConfig writerDs = odsDatasourceConfigService.getById(tunnel.getWriterDatasourceId());
                    jdbcInfoDto.setJdbcUrlForIn(writerDs.getDsUrl());
                    jdbcInfoDto.setDbUserNameForIn(writerDs.getDsUsername());
                    jdbcInfoDto.setDbPasswdForIn(writerDs.getDsPwd());
                    jdbcInfoDto.setDsConfigId(tunnel.getWriterDatasourceId());
                    dbType = writerDs.getDbType();
                }
                // 表以及对应的sql信息
                assembleTableInfoMessage(tunnel, jdbcInfoDto,"HDFS".equals(dbType));
            }
            tunnelMessageDTO.setJdbcInfoDto(jdbcInfoDto);
        }
        return tunnelMessageDTO;
    }


    private void assembleTableInfoMessage(ConvTunnel tunnel, JdbcInfoDto jdbcInfoDto,Boolean isHive){
        // 库表采集范围和sql查询语句
        List<TableInfoDto> tableInfoDtoList = new ArrayList<>();
        List<String> tableList = Arrays.asList(tunnel.getCollectRange().split(","));
        // 增量字段
        List<ConvCollectField> collectFieldList = collectFieldService.list(new LambdaQueryWrapper<ConvCollectField>()
                        .in(ConvCollectField::getTableName, tableList)
                        .eq(ConvCollectField::getTunnelId, tunnel.getId())
                        .eq(ConvCollectField::getSystemCode, tunnel.getSysCode()));
        Map<String, List<OriginalModelColumn>> modelColumnMap = new HashMap<>(collectFieldList.size());
        Map<String, String> hdfsMap = new HashMap<>();
        Map<String, String> hiveConfigMap = new HashMap<>();
        if (isHive){
            List<Long> modelIdList = new ArrayList<>();
            for (ConvCollectField collectField : collectFieldList) {
                List<OriginalModelColumn> modelColumns = odsModelService.getColumnList(collectField.getTableName(), collectField.getSystemCode());
                modelColumnMap.put(collectField.getTableName(),modelColumns);
                modelIdList.add(modelColumns.get(0).getModelId());
            }
            List<OriginalModel> modelList = odsModelService.getModelList(modelIdList);
            hdfsMap = modelList.stream().filter(e -> StringUtils.isNotEmpty(e.getStoragePath())).collect(Collectors.toMap(OriginalModel::getNameEn, OriginalModel::getStoragePath));
            //查询hive配置
            List<ModelConfig> modelConfigs = modelConfigService.list(new LambdaQueryWrapper<ModelConfig>().in(ModelConfig::getModelId, modelIdList));
            hiveConfigMap = modelConfigs.stream().filter(e -> StringUtils.isNotEmpty(e.getTableType())).collect(Collectors.toMap(ModelConfig::getTableName, ModelConfig::getTableType));

        }
        Map<String, String> finalHdfsMap = hdfsMap;
        Map<String, String> finalHiveConfigMap = hiveConfigMap;
        collectFieldList.forEach(model -> {
            TableInfoDto tableInfoDto = new TableInfoDto();
            tableInfoDto.setTableName(model.getTableName());
            tableInfoDto.setSqlQuery(model.getQuerySql());
            tableInfoDto.setHdfsPath(finalHdfsMap.get(model.getTableName()));
            tableInfoDto.setHiveFileType(finalHiveConfigMap.get(model.getTableName()));
            tableInfoDto.setWriterColumns(isHive ? doGetHiveColumns(modelColumnMap,model) : model.getColumnField() );
            if (tunnel.getColType().equals(TunnelColTypeEnum.FREQUENCY_INCREMENT.getValue())){
                // 1-时间 2-序列
                tableInfoDto.setSeqField(model.getConditionField());
                tableInfoDto.setSeqFieldType(model.getConditionFieldType());
                Map<String, String> fieldMap = new HashMap<>();
                getIncrFieldMap(tunnel, model, fieldMap);
                tableInfoDto.setIncrTimeMap(fieldMap);
            }
            tableInfoDtoList.add(tableInfoDto);
        });
        jdbcInfoDto.setTableInfoDtoList(tableInfoDtoList);
    }

    /**
     * 创建hive库columns映射关系
     */
    private String doGetHiveColumns(Map<String, List<OriginalModelColumn>> modelColumnMap,ConvCollectField field) {
        List<OriginalModelColumn> originalModelColumns = modelColumnMap.get(field.getTableName());
        if (CollectionUtil.isEmpty(originalModelColumns)){
            return null;
        }
        if (StringUtils.isNotEmpty(field.getColumnField())){
            List<String> existNames = Arrays.asList(field.getColumnField().split(","));
            originalModelColumns = originalModelColumns.stream().filter(column -> existNames.contains(column.getNameEn()))
                    .sorted(Comparator.comparing(OriginalModelColumn::getSeqNo)).collect(Collectors.toList());
        }
        StringBuilder sb = new StringBuilder();
        for (OriginalModelColumn modelColumn : originalModelColumns) {
            sb.append("{\"name\":\"").append(modelColumn.getNameEn())
                    .append("\",\"type\":\"").append(transformDataType(modelColumn.getFieldType()))
                    .append("\"}").append(",\n");
        }
        return sb.substring(0, sb.length() - 2);
    }

    private String transformDataType(String fieldType) {
        fieldType = fieldType.toLowerCase();
        String transformStr;
       switch (fieldType){
           case "varchar":
               transformStr = "VARCHAR";
               break;
           case "int4":
               transformStr = "INT";
               break;
           case "int8":
               transformStr = "BIGINT";
               break;
           case "datetime":
               transformStr = "DATE";
               break;
           case "numeric":
               transformStr = "DOUBLE";
               break;
           case "timestamp":
               transformStr = "TIMESTAMP";
               break;
           default:
               transformStr = fieldType;
               break;
       }
       return transformStr;
    }

    private void getIncrFieldMap(ConvTunnel tunnel, ConvCollectField field, Map<String, String> fieldMap){
        String column = field.getConditionField();
        ConvCollectIncrTime collectIncrTime = incrTimeService.getOne(new LambdaQueryWrapper<ConvCollectIncrTime>()
                .eq(ConvCollectIncrTime::getTunnelId, tunnel.getId())
                .eq(ConvCollectIncrTime::getIncrField, column)
                .eq(ConvCollectIncrTime::getTableName, field.getTableName()));
        if (ObjectUtil.isNotNull(collectIncrTime)) {
            // 已进行增量采集，使用最新采集时间
            if (SeqFieldTypeEnum.TIME.getValue().equals(field.getConditionFieldType())){
                fieldMap.put(column, collectIncrTime.getLatestTime());
            }else {
                fieldMap.put(column, collectIncrTime.getLatestSeq());
            }
        }else {
            // 第一次增量采集，使用初始点位
            if (SeqFieldTypeEnum.TIME.getValue().equals(field.getConditionFieldType())){
                LocalDateTime timePoint = field.getTimePoint();
                String format = DateUtil.format(timePoint, "yyyy-MM-dd HH:mm:ss");
                fieldMap.put(column, format);
            }else {
                fieldMap.put(column, String.valueOf(field.getNumPoint()));
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
