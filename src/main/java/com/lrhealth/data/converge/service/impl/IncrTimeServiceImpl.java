package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.enums.SeqFieldTypeEnum;
import com.lrhealth.data.converge.dao.entity.*;
import com.lrhealth.data.converge.dao.service.*;
import com.lrhealth.data.converge.model.dto.IncrSequenceDto;
import com.lrhealth.data.converge.service.IncrTimeService;
import com.lrhealth.data.converge.service.KafkaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

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
    private ConvOriginalTableService originalTableService;
    @Resource
    private ConvOriginalColumnService originalColumnService;
    @Resource
    private ConvCollectIncrTimeService convCollectIncrTimeService;
    @Resource
    private KafkaService kafkaService;

    @Async
    @Override
    public void updateTableLatestTime(Long xdsId, String endIndex) {
        if (CharSequenceUtil.isBlank(endIndex)){
            log.error("[{}]没有endIndex!!!!", xdsId);
            return;
        }
        log.info("<<<开始更新xds[{}]的最新采集时间！>>>", xdsId);
        Xds xds = xdsService.getById(xdsId);
        ConvTask convTask = taskService.getById(xds.getConvTaskId());
        if (ObjectUtil.isNull(convTask)){
            return;
        }
        ConvTunnel tunnel = tunnelService.getById(convTask.getTunnelId());
        ConvOriginalTable table = originalTableService.getOne(new LambdaQueryWrapper<ConvOriginalTable>()
                .eq(ConvOriginalTable::getNameEn, xds.getOdsTableName())
                .eq(ConvOriginalTable::getSysCode, xds.getSysCode()));
        if (ObjectUtil.isEmpty(table)){
            log.error("[{}]原始表不存在！", xds.getOdsTableName());
            return;
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
                update.setLatestTime(latestValue);
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
}
