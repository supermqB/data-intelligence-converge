package com.lrhealth.data.converge.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTask;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultCdc;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskResultCdcService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTunnelService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author yuanbaiyu
 * @since 2023/10/13 16:01
 */
@Slf4j
@Component
public class CdcMetricsConsumer {

    @Resource
    private ConvTunnelService convTunnelService;
    @Resource
    private ConvTaskService convTaskService;
    @Resource
    private ConvTaskResultCdcService convTaskResultCdcService;

    @KafkaListener(topics = "${spring.kafka.topic.metrics}", groupId = "metrics", containerFactory = "kafkaListenerContainerFactory")
    public void consumer(String message) {
        log.info("{}", message);

        List<CdcRecord> records = JSON.parseArray(message, CdcRecord.class);
        if (CollUtil.isEmpty(records)) {
            return;
        }
        Map<String, List<CdcRecord>> map = new HashMap<>();
        for (CdcRecord record : records) {
            String key = StrUtil.concat(true, record.getTable(), record.getJid());
            List<CdcRecord> list = map.getOrDefault(key, Lists.newArrayList());
            list.add(record);
            map.put(key, list);
        }

        CdcRecord first = records.get(0);

        for (Map.Entry<String, List<CdcRecord>> entry : map.entrySet()) {
            List<CdcRecord> v = entry.getValue();
            ConvTunnel tunnel = updateConvTunnel(first);
            if (tunnel == null) {
                continue;
            }
            ConvTask task = updateConvTask(first, tunnel);
            if (task == null) {
                continue;
            }
            flushConvCdcRecord(task, first, v);
        }
    }

    private void flushConvCdcRecord(ConvTask task, CdcRecord first, List<CdcRecord> records) {
        // @formatter:off
        ConvTaskResultCdc cdc = ConvTaskResultCdc.builder()
            .taskId(Long.valueOf(task.getId()))
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
        }

        for (CdcRecord record : records) {
            Integer addCount = cdc.getAddCount();
            Integer deleteCount = cdc.getDeleteCount();
            Integer updateCount = cdc.getUpdateCount();

            switch (record.getOperation()) {
                case "insert":
                    cdc.setAddCount((addCount == null ? 0 : addCount) + 1 + origin.getAddCount());
                    break;
                case "update":
                    cdc.setUpdateCount((updateCount == null ? 0 : updateCount) + 1 + origin.getUpdateCount());
                    break;
                case "delete":
                    cdc.setDeleteCount((deleteCount == null ? 0 : deleteCount) + 1 + origin.getDeleteCount());
                    break;
                default:
                    break;
            }
            cdc.setDataCount(cdc.getDeleteCount() + cdc.getAddCount() + cdc.getUpdateCount() + origin.getDataCount());
        }
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
        Long fedTaskId = first.taskId;
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

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CdcRecord {
        private String database;
        private String schema;
        private String table;
        private String operation;
        private HashMap<String, Object> value;
        private String jid;
        private Long tunnelId;
        private Long taskId;
    }

}
