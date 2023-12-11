package com.lrhealth.data.converge.scheduled;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.lrhealth.data.converge.cache.Cache;
import com.lrhealth.data.converge.dao.service.ConvMonitorService;
import com.lrhealth.data.converge.model.dto.MonitorDTO;
import com.lrhealth.data.converge.model.dto.MonitorMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 汇聚监测信息定时任务
 *
 * @author admin
 */
@Slf4j
@Component
@EnableScheduling
public class ConvMonitorTask {
    @Resource
    private ConvMonitorService convMonitorService;
    @Resource
    private Cache convCache;

    private static final String FE_NODE_PREFIX = "fe_node:";

    private static final int TIME_OUT_THRESHOLD = 10;

    /**
     * 前置机异常状态监测定时任务
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void monitorConvTask() {
        List<String> allKeysByPrefix = convCache.getAllByPrefix(FE_NODE_PREFIX);
        if (CollectionUtils.isEmpty(allKeysByPrefix)) {
            return;
        }
        for (String keyByPrefix : allKeysByPrefix) {
            Object cacheValue = convCache.getObject(keyByPrefix);
            if (cacheValue == null) {
                continue;
            }
            MonitorDTO monitorInfo = null;
            try {
                monitorInfo = JSON.parseObject(JSON.toJSONString(cacheValue), MonitorDTO.class);
            } catch (Exception e) {
                log.info("定时任务异常信息处理: keyByPrefix --> {}, cacheValue --> {},e = {}", keyByPrefix, cacheValue, e.getMessage());
                continue;
            }
            processTimeOutMonitor(keyByPrefix, monitorInfo);
        }
    }

    /**
     * 处理超时的监测信息
     *
     * @param cacheKey 缓存key
     * @param monitor  监测信息
     */
    private void processTimeOutMonitor(String cacheKey, MonitorDTO monitor) {
        if (Objects.isNull(monitor)) {
            return;
        }
        long between = DateUtil.between(monitor.getUpdateTime(), new Date(), DateUnit.MINUTE);
        if (between > TIME_OUT_THRESHOLD) {
            MonitorMsg monitorMsg = new MonitorMsg();
            monitorMsg.setStatus(null);
            monitorMsg.setStatus(false);
            MonitorMsg.MsgTypeEnum msgTypeEnum = MonitorMsg.MsgTypeEnum.valueOf(monitor.getMonitorType());
            monitorMsg.setMsg(msgTypeEnum.getMsgTypeDesc() + "异常时间超过" + TIME_OUT_THRESHOLD + "分钟");
            //写异常信息 删除缓存key 防止断连前置机信息一直存在缓存
            convMonitorService.processConvMonitor(monitor, monitorMsg);
            convCache.removeObject(cacheKey);
        }
    }

}
