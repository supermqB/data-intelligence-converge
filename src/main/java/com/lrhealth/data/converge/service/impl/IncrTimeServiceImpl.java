package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.enums.SeqFieldTypeEnum;
import com.lrhealth.data.converge.common.util.SqlExecUtil;
import com.lrhealth.data.converge.common.util.StringUtils;
import com.lrhealth.data.converge.dao.entity.*;
import com.lrhealth.data.converge.dao.mapper.StdOriginalModelMapper;
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
import java.sql.Timestamp;
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
    private StdOriginalModelMapper stdOriginalModelMapper;
    @Resource
    private KafkaService kafkaService;

    @Async
    @Override
    public void updateTableLatestTime(Long xdsId, String endIndex) {
        log.info("<<<开始更新xds[{}]的最新采集时间！>>>", xdsId);
        Xds xds = xdsService.getById(xdsId);
        ConvTask convTask = taskService.getById(xds.getConvTaskId());
        if (ObjectUtil.isNull(convTask)){
            return;
        }
        ConvTunnel tunnel = tunnelService.getById(convTask.getTunnelId());
        ConvOriginalTable table = originalTableService.getOne(new LambdaQueryWrapper<ConvOriginalTable>()
                .eq(ConvOriginalTable::getModelName, xds.getOdsTableName())
                .eq(ConvOriginalTable::getSysCode, xds.getSysCode()));
        if (ObjectUtil.isEmpty(table)){
            log.info("[{}]原始表不存在！", xds.getOdsModelName());
            return;
        }
        ConvOdsDatasourceConfig dsConfig = datasourceConfigService.getById(xds.getDsConfigId());
        //若使用HDFS存储 通过原始模型 查询对应的hive数据源
        if (dsConfig != null && "HDFS".equals(dsConfig.getDbType())){
            StdOriginalModel stdOriginalModel = stdOriginalModelMapper.selectOne(new LambdaQueryWrapper<StdOriginalModel>()
                    .eq(StdOriginalModel::getId, table.getModelId())
                    .eq(StdOriginalModel::getDelFlag, 0));
            if (stdOriginalModel != null && StringUtils.isNotEmpty(stdOriginalModel.getConvDsConfId())){
                dsConfig = datasourceConfigService.getById(Integer.valueOf(stdOriginalModel.getConvDsConfId()));
            }
        }
        List<ConvOriginalColumn> convOriginalColumns = originalColumnService.list(new LambdaQueryWrapper<ConvOriginalColumn>()
                .eq(ConvOriginalColumn::getTableId, table.getId())
                .ne(ConvOriginalColumn::getIncrFlag, "0"));
        if (CollUtil.isEmpty(convOriginalColumns)){
            return;
        }
        convOriginalColumns.forEach(column -> updateCollectIncrTime(tunnel, xds, column, endIndex));
    }

    private void updateCollectIncrTime(ConvTunnel tunnel, Xds xds, ConvOriginalColumn column, String latestValue){
        ConvCollectIncrTime collectIncrTime = convCollectIncrTimeService.getOne(new LambdaQueryWrapper<ConvCollectIncrTime>()
                .eq(ConvCollectIncrTime::getTunnelId, tunnel.getId())
                .eq(ConvCollectIncrTime::getTableName, xds.getOdsTableName())
                .eq(ConvCollectIncrTime::getIncrField, column.getNameEn()));
        String incrType = column.getIncrFlag();
        if (ObjectUtil.isNotNull(collectIncrTime)) {
            ConvCollectIncrTime update = ConvCollectIncrTime.builder()
                    .id(collectIncrTime.getId())
                    .tunnelId(collectIncrTime.getTunnelId())
                    .tableName(collectIncrTime.getTableName())
                    .incrField(collectIncrTime.getIncrField())
                    .updateTime(LocalDateTime.now())
                    .build();
            if (SeqFieldTypeEnum.TIME.getValue().equals(incrType)){
                Timestamp value = Timestamp.valueOf(latestValue);
                update.setLatestTime(value.toString());
            }else {
                update.setLatestSeq(latestValue);
            }
            convCollectIncrTimeService.updateById(update);
            // 给前置机更新最新采集时间
            sendLastedIncrTimeToFep(update,incrType,tunnel);
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
            build.setLatestTime(latestValue);
        }else {
            build.setLatestSeq(latestValue);
        }
        convCollectIncrTimeService.saveOrUpdate(build);
        // 给前置机更新最新采集时间
        sendLastedIncrTimeToFep(build,incrType,tunnel);
    }

    /**
     * 给前置机更新最新采集时间/最大序列号
     */
    private void sendLastedIncrTimeToFep(ConvCollectIncrTime build,String incrType,ConvTunnel tunnel){
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
