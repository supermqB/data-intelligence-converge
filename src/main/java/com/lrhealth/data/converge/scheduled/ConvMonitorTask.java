package com.lrhealth.data.converge.scheduled;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.lrhealth.data.converge.cache.Cache;
import com.lrhealth.data.converge.dao.service.ConvMonitorService;
import com.lrhealth.data.converge.model.dto.MonitorMsg;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvFeNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    /**
     * 前置机异常状态监测定时任务
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void monitorConvTask() {
        List<String> allFeByPrefix = convCache.getAllByPrefix(FE_NODE_PREFIX);
        if (CollectionUtils.isEmpty(allFeByPrefix)) {
            return;
        }
        //获取缓存中所有前置机当前信息
        List<ConvFeNode> allFeNodes = new ArrayList<>();
        for (String feByPrefix : allFeByPrefix) {
            Object cacheValue = convCache.getObject(feByPrefix);
            if (cacheValue == null) {
                continue;
            }
            List<ConvFeNode> feNodeList = JSON.parseArray(JSON.toJSONString(cacheValue), ConvFeNode.class);
            if (CollectionUtils.isNotEmpty(feNodeList)){
                allFeNodes.addAll(feNodeList);
            }
        }
        if (CollectionUtils.isNotEmpty(allFeNodes)) {
            for (ConvFeNode feNode : allFeNodes) {
                long between = DateUtil.between(feNode.getUpdateTime(), new Date(), DateUnit.MINUTE);
                if (between > 10) {
                    MonitorMsg monitorMsg = new MonitorMsg();
                    monitorMsg.setMsg(feNode.getName() + "已离线" + between + "分钟");
                    convMonitorService.processConvMonitor(feNode, monitorMsg);
                }
            }
        }
    }
}
