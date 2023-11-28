package com.lrhealth.data.converge.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTask;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultCdc;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static cn.hutool.core.text.CharSequenceUtil.concat;

/**
 * @author yuanbaiyu
 * @since 2023/10/13 16:01
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "cdc.metrics", havingValue = "true")
public class CdcMetricsConsumer extends CdcCon {

    private static final String GROUP_ID = "metrics";

    @KafkaListener(topics = "${spring.kafka.topic.metrics}", groupId = GROUP_ID, containerFactory = "kafkaListenerContainerFactory")
    public void consumer(String message) {
        List<CdcRecord> records = parseMessage(message, "insert", "update", "delete");
        records = records.stream().filter(v -> StrUtil.isNotBlank(v.getJid()) && StrUtil.isNotBlank(v.getTable())).collect(Collectors.toList());
        if (CollUtil.isEmpty(records)) {
            return;
        }
        Map<String, List<CdcRecord>> map = new HashMap<>();
        for (CdcRecord cdcRecord : records) {
            String key = concat(true, cdcRecord.getTable(), cdcRecord.getJid());
            List<CdcRecord> list = map.getOrDefault(key, Lists.newArrayList());
            list.add(cdcRecord);
            map.put(key, list);
        }

        for (Map.Entry<String, List<CdcRecord>> entry : map.entrySet()) {
            records = entry.getValue();
            CdcRecord first = records.get(0);

            ConvTunnel tunnel = updateConvTunnel(first);
            if (tunnel == null) {
                continue;
            }
            ConvTask task = updateConvTask(first, tunnel);
            if (task != null) {
                flushConvCdcRecord(task, first, records);
            }
        }
    }

    private void flushConvCdcRecord(ConvTask task, CdcRecord first, List<CdcRecord> records) {
        // @formatter:off
        ConvTaskResultCdc cdc = ConvTaskResultCdc.builder()
            .taskId(Long.valueOf(task.getId()))
            .tableName(first.getTable())
            .flinkJobId(first.getJid())
            .delFlag(0)
            .build();

        LambdaQueryWrapper<ConvTaskResultCdc> wrapper = new LambdaQueryWrapper<ConvTaskResultCdc>()
            .eq(ConvTaskResultCdc::getFlinkJobId, first.getJid())
            .eq(ConvTaskResultCdc::getTableName, first.getTable());
        // @formatter:on

        List<ConvTaskResultCdc> cdcList = convTaskResultCdcService.list(wrapper);
        ConvTaskResultCdc origin = ConvTaskResultCdc.zero();
        if (CollUtil.size(cdcList) > 1) {
            convTaskResultCdcService.remove(wrapper);
            Optional<ConvTaskResultCdc> optional = cdcList.stream().max(Comparator.comparing(ConvTaskResultCdc::getUpdateTime));
            if (optional.isPresent()) {
                origin = optional.get();
            }
        } else if (CollUtil.size(cdcList) == 1) {
            origin = cdcList.get(0);
        }

        for (CdcRecord cdcRecord : records) {
            switch (cdcRecord.getOperation()) {
                case "insert":
                    cdc.setAddCount(add(cdc.getAddCount(), 1));
                    break;
                case "update":
                    cdc.setUpdateCount(add(cdc.getUpdateCount(), 1));
                    break;
                case "delete":
                    cdc.setDeleteCount(add(cdc.getDeleteCount(), 1));
                    break;
                default:
                    break;
            }
        }
        cdc.setDeleteCount(add(cdc.getDeleteCount(), origin.getDeleteCount()));
        cdc.setAddCount(add(cdc.getAddCount(), origin.getAddCount()));
        cdc.setUpdateCount(add(cdc.getUpdateCount(), origin.getUpdateCount()));
        cdc.setDataCount(add(cdc.getDeleteCount(), cdc.getUpdateCount(), cdc.getAddCount()));

        convTaskResultCdcService.saveOrUpdate(cdc, wrapper);
    }

    private ConvTunnel updateConvTunnel(CdcRecord first) {
        Long tunnelId = first.getTunnelId();

        ConvTunnel tunnel = convTunnelService.getById(tunnelId);
        if (tunnel == null) {
            log.error("Tunnel does not exist. tunnelId: {}", tunnelId);
            return null;
        }
        tunnel.setStatus(7);
        convTunnelService.updateById(tunnel);
        return tunnel;
    }

    private ConvTask updateConvTask(CdcRecord first, ConvTunnel tunnel) {
        Long fedTaskId = first.getTaskId();
        Long taskId = first.getTaskId();
        Long tunnelId = first.getTunnelId();

        // @formatter:off
        ConvTask task = convTaskService.getOne(new LambdaQueryWrapper<ConvTask>()
            .eq(ConvTask::getFedTaskId, fedTaskId)
            .eq(ConvTask::getTunnelId, tunnelId), false);
        if (task == null) {
            log.error("Task does not exist. tunnelId:[{}], fedTaskId:[{}]", tunnelId, fedTaskId);
            return null;
        }

        task.setStatus(7);
        task.setFedTaskId(Math.toIntExact(fedTaskId));
        task.setTunnelId(tunnelId);
        task.setName(tunnel.getName() + "_任务" + taskId);
        task.setSysCode(tunnel.getSysCode());
        task.setOrgCode(tunnel.getOrgCode());
        task.setConvergeMethod(tunnel.getConvergeMethod());

        convTaskService.saveOrUpdate(task, new LambdaQueryWrapper<ConvTask>()
            .eq(ConvTask::getTunnelId, tunnelId)
            .eq(ConvTask::getFedTaskId, fedTaskId));
        // @formatter:on
        return task;
    }

    private int add(Integer... arr) {
        int sum = 0;
        for (Integer i : arr) {
            sum += Optional.ofNullable(i).orElse(0);
        }
        return sum;
    }

}
