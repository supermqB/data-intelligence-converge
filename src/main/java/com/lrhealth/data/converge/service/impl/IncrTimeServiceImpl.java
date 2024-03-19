package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.enums.SeqFieldTypeEnum;
import com.lrhealth.data.converge.common.util.SqlExecUtil;
import com.lrhealth.data.converge.dao.entity.*;
import com.lrhealth.data.converge.dao.service.*;
import com.lrhealth.data.converge.model.dto.DataSourceDto;
import com.lrhealth.data.converge.model.dto.IncrSequenceDto;
import com.lrhealth.data.converge.service.IncrTimeService;
import com.lrhealth.data.converge.service.KafkaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.System;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author jinmengyu
 * @date 2024-01-17
 */
@Slf4j
@Service
public class IncrTimeServiceImpl implements IncrTimeService {

    @Resource
    private XdsService xdsService;
    @Resource
    private ConvTaskService taskService;
    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private ConvOdsDatasourceConfigService datasourceConfigService;
    @Resource
    private ConvOriginalTableService originalTableService;
    @Resource
    private ConvOriginalColumnService originalColumnService;
    @Resource
    private ConvCollectIncrTimeService convCollectIncrTimeService;
    @Resource
    private KafkaService kafkaService;

    @Async
    @Override
    public void updateTableLatestTime(Long xdsId) {
        log.info("<<<开始更新xds[{}]的最新采集时间！>>>", xdsId);
        Xds xds = xdsService.getById(xdsId);
        ConvTask convTask = taskService.getById(xds.getConvTaskId());
        if (ObjectUtil.isNull(convTask)){
            return;
        }
        ConvTunnel tunnel = tunnelService.getById(convTask.getTunnelId());
        ConvOdsDatasourceConfig dsConfig = datasourceConfigService.getById(xds.getDsConfigId());
        DataSourceDto dto = DataSourceDto.builder()
                .driver(dsConfig.getDsDriverName())
                .jdbcUrl(dsConfig.getDsUrl())
                .username(dsConfig.getDsUsername())
                .password(dsConfig.getDsPwd()).build();
        ConvOriginalTable table = originalTableService.getOne(new LambdaQueryWrapper<ConvOriginalTable>()
                .eq(ConvOriginalTable::getConvDsConfId, tunnel.getReaderDatasourceId())
                .eq(ConvOriginalTable::getNameEn, xds.getOdsModelName())
                .eq(ConvOriginalTable::getSysCode, xds.getSysCode()));
        if (ObjectUtil.isEmpty(table)){
            return;
        }
        List<ConvOriginalColumn> convOriginalColumns = originalColumnService.list(new LambdaQueryWrapper<ConvOriginalColumn>()
                .eq(ConvOriginalColumn::getTableId, table.getId())
                .ne(ConvOriginalColumn::getIncrFlag, "0"));
        if (CollUtil.isEmpty(convOriginalColumns)){
            return;
        }
        convOriginalColumns.forEach(column -> {
            String sql = getTaskLatestTimeSql(xds.getId(), xds.getOdsTableName(), column.getNameEn());
            List<Map<String, Object>> mapList = SqlExecUtil.execSql(sql, dto);
            if (CollUtil.isNotEmpty(mapList)){
                Object latestValue = mapList.get(0).get(column.getNameEn());
                updateCollectIncrTime(tunnel, xds, column, latestValue);
            }
        });
    }

    private void updateCollectIncrTime(ConvTunnel tunnel, Xds xds, ConvOriginalColumn column, Object latestValue){
        ConvCollectIncrTime collectIncrTime = convCollectIncrTimeService.getOne(new LambdaQueryWrapper<ConvCollectIncrTime>()
                .eq(ConvCollectIncrTime::getTunnelId, tunnel.getId())
                .eq(ConvCollectIncrTime::getTableName, xds.getOdsTableName())
                .eq(ConvCollectIncrTime::getIncrField, column.getNameEn()));
        String incrType = column.getIncrFlag();
        if (ObjectUtil.isNotNull(collectIncrTime)) {
            ConvCollectIncrTime update = ConvCollectIncrTime.builder()
                    .id(collectIncrTime.getId())
                    .updateTime(LocalDateTime.now())
                    .build();
            if (SeqFieldTypeEnum.TIME.getValue().equals(incrType)){
                DateTime dateTime = DateUtil.date((Date) latestValue);
                update.setLatestTime(dateTime.toString());
            }else {
                update.setLatestSeq(latestValue.toString());
            }
            convCollectIncrTimeService.updateById(update);
            return;
        }
        ConvCollectIncrTime build = ConvCollectIncrTime.builder()
                .tunnelId(tunnel.getId())
                .tableName(xds.getOdsTableName())
                .incrField(column.getNameEn())
                .incrFieldType(incrType)
                .createTime(LocalDateTime.now())
                .orgCode(xds.getOrgCode())
                .sysCode(xds.getSysCode())
                .build();
        if (SeqFieldTypeEnum.TIME.getValue().equals(incrType)){
            DateTime dateTime = DateUtil.date((Date) latestValue);
            build.setLatestTime(dateTime.toString());
        }else {
            build.setLatestSeq(latestValue.toString());
        }
        convCollectIncrTimeService.saveOrUpdate(build);

        // 给前置机更新最新采集时间
        IncrSequenceDto incrSequenceDto = IncrSequenceDto.builder()
                .tunnelId(build.getTunnelId())
                .tableName(build.getTableName())
                .seqField(build.getIncrField())
                .incrSequence(SeqFieldTypeEnum.TIME.getValue().equals(incrType) ? build.getLatestTime() : build.getLatestSeq())
                .build();
        kafkaService.updateFepIncrSequence(incrSequenceDto, tunnel);
    }




    private String getTaskLatestTimeSql(Long xdsId, String tableName, String businessField){
        return "SELECT " + businessField + " FROM " + tableName +
                " WHERE xds_id = '" + xdsId + "' ORDER BY " + businessField + " DESC LIMIT 1";
    }

    public static void main(String[] args) {
        String sql = "SELECT " + "FILL_DATE" + " FROM " + "DI_RSG_BEDS_INFO" +
                " WHERE xds_id = '" + "1747433178191933440" + "' ORDER BY " + "FILL_DATE" + " LIMIT 1";
        DataSourceDto dto = DataSourceDto.builder()
                .driver("com.oceanbase.jdbc.Driver")
                .jdbcUrl("jdbc:oceanbase://172.16.29.68:2883/ods_test_gy")
                .username("root@rdcp_std")
                .password("LR_rdcp@2023").build();
        List<Map<String, Object>> mapList = SqlExecUtil.execSql(sql, dto);
        Object fillDate = mapList.get(0).get("FILL_DATE");
        String dateTime = DateUtil.formatDateTime((Date) fillDate);
        System.out.println(dateTime);
    }
}
