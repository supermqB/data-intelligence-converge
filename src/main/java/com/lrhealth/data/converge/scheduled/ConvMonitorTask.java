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
import java.util.stream.Collectors;

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
     * 前置机监测信息同步定时任务
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void monitorConvTask() {
        List<String> allFeByPrefix = convCache.getAllByPrefix(FE_NODE_PREFIX);
        if (CollectionUtils.isEmpty(allFeByPrefix)) {
            return;
        }
        //获取当前离线机器信息
        List<ConvFeNode> offLineFeNodes = new ArrayList<>();
        for (String feByPrefix : allFeByPrefix) {
            Object cacheValue = convCache.getObject(feByPrefix);
            if (cacheValue == null) {
                continue;
            }
            List<ConvFeNode> feNodeList = JSON.parseArray(JSON.toJSONString(cacheValue), ConvFeNode.class);
            List<ConvFeNode> nodes = feNodeList.stream().filter(ele -> ele.getState() == 0).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(nodes)){
                offLineFeNodes.addAll(nodes);
            }
        }
        if (CollectionUtils.isEmpty(offLineFeNodes)) {
            return;
        }
        for (ConvFeNode offLineFeNode : offLineFeNodes) {
            long between = DateUtil.between(offLineFeNode.getUpdateTime(), new Date(), DateUnit.MINUTE);
            if (between > 1) {
                MonitorMsg monitorMsg = new MonitorMsg();
                monitorMsg.setMsg(offLineFeNode.getName() + "已离线" + between + "分钟");
                convMonitorService.processConvMonitor(offLineFeNode, monitorMsg);
            }
        }

    }
}
