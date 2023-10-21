package com.lrhealth.data.converge.scheduled.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.common.util.DateUtil;
import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import com.lrhealth.data.converge.common.enums.TunnelStatusEnum;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.scheduled.service.convergeTaskService;
import com.lrhealth.data.converge.scheduled.utils.SchedulerUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * @author jinmengyu
 * @date 2023-09-14
 */
@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

    private static Logger log = LoggerFactory.getLogger(SchedulerConfig.class);
    @Resource
    private ConvTunnelService frontendTunnelService;
    @Resource
    private convergeTaskService convergeTaskService;

    private ScheduledTaskRegistrar taskRegistrar;
    private Set<ScheduledFuture<?>> scheduledFutures = null;
    private Map<String, ScheduledFuture<?>> taskFutures = new ConcurrentHashMap<>();

    /**
     * 这个方法在Spring初始化的时候会帮我们执行，这里也会拉取数据库内需要执行的任务，进行添加到定时器里。
     * @param scheduledTaskRegistrar
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        List<TriggerTask> list= new ArrayList<>();
        //查询出来当前数据库中存储的所有有效的任务
        LambdaQueryWrapper<ConvTunnel> wrapper = new LambdaQueryWrapper<ConvTunnel>()
            .eq(ConvTunnel::getDelFlag, 0)
                .eq(ConvTunnel::getConvergeMethod, TunnelCMEnum.LIBRARY_TABLE.getCode())
                .eq(ConvTunnel::getFrontendId, -1)
                .in(ConvTunnel::getStatus, TunnelStatusEnum.SCHEDULING.getValue(), TunnelStatusEnum.PROCESSING.getValue());
        List<ConvTunnel> tunnels = frontendTunnelService.list(wrapper);
        //循环添加任务
        tunnels.forEach(t->{
            TriggerTask triggerTask = createTriggerTask(t);
            list.add(triggerTask);
        });
        //将任务列表注册到定时器
        scheduledTaskRegistrar.setTriggerTasksList(list);
        this.taskRegistrar = scheduledTaskRegistrar;
    }

    public TriggerTask createTriggerTask(ConvTunnel tunnel){
        return new TriggerTask(()->{
            // 任务执行
            convergeTaskService.taskExec(null, tunnel.getId());
            log.info("执行定时任务：时间：{}， 任务：{}", DateUtil.getNow(), tunnel.getId());
        },triggerContext -> {
            log.info("开始执行Cron: {}, tunnelId =  {}", tunnel.getCronStr(), tunnel.getId());
            return new CronTrigger(tunnel.getCronStr()).nextExecutionTime(triggerContext);
        });
    }


    /**
     * 添加任务
     * @param tunnelId
     * @param triggerTask
     */
    public void addTask(String tunnelId, TriggerTask triggerTask) {
        //如果定时任务id已存在，则取消原定时器，从新创建新定时器，这里也是个更新定时任务的过程。
        if (taskFutures.containsKey(tunnelId)) {
            log.info("the tunnelId [" + tunnelId + "] 取消，重新添加");
            cancelTriggerTask(tunnelId);
        }
        TaskScheduler scheduler = taskRegistrar.getScheduler();
        ScheduledFuture<?> future = scheduler.schedule(triggerTask.getRunnable(), triggerTask.getTrigger());
        getScheduledFutures().add(future);
        taskFutures.put(tunnelId, future);
    }

    /**
     * 获取任务列表
     */
    private Set<ScheduledFuture<?>> getScheduledFutures() {
        if (scheduledFutures == null) {
            try {
                scheduledFutures = (Set<ScheduledFuture<?>>) SchedulerUtil.getProperty(taskRegistrar, "scheduledTasks");
            } catch (NoSuchFieldException e) {
                log.error("log error,{}", ExceptionUtils.getStackTrace(e));
            }
        }
        return scheduledFutures;
    }
    /**
     * 取消任务
     */
    public void cancelTriggerTask(String tunnelId) {
        ScheduledFuture<?> future = taskFutures.get(tunnelId);
        if (future != null) {
            future.cancel(true);
        }
        taskFutures.remove(tunnelId);
        getScheduledFutures().remove(future);
    }
}
