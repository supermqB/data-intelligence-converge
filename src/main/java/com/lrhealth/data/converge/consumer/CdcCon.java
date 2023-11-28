package com.lrhealth.data.converge.consumer;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskResultCdcService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTunnelService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static cn.hutool.core.text.CharSequenceUtil.containsAnyIgnoreCase;
import static cn.hutool.core.text.CharSequenceUtil.isBlank;

/**
 * @author yuanbaiyu
 * @since 2023/11/15 16:23
 */
@Slf4j
public class CdcCon {

    @Resource
    protected ConvTunnelService convTunnelService;
    @Resource
    protected ConvTaskService convTaskService;
    @Resource
    protected ConvTaskResultCdcService convTaskResultCdcService;

    @NotNull
    protected static List<CdcRecord> parseMessage(String message, String... operations) {
        List<CdcRecord> records = new ArrayList<>();
        if (isBlank(message)) {
            return records;
        }
        if (JSONUtil.isTypeJSONArray(message)) {
            // @formatter:off
            records = JSON.parseArray(message, CdcRecord.class)
                .stream()
                .filter(v -> containsAnyIgnoreCase(v.getOperation(), operations))
                .collect(Collectors.toList());
            // @formatter:on
        } else if (JSONUtil.isTypeJSON(message)) {
            records.add(JSON.parseObject(message, CdcRecord.class));
        }
        return records;
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
        private TreeMap<String, Object> value;
        private String jid;
        private Long tunnelId;
        private Long taskId;
    }

}
