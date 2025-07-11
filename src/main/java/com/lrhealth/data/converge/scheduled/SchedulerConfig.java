package com.lrhealth.data.converge.scheduled;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import com.lrhealth.data.converge.common.enums.TunnelStatusEnum;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.service.ActiveInterfaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

/**
 * @author jinmengyu
 * @date 2023-09-14
 */
@Slf4j
@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {

    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private ActiveInterfaceService activeInterfaceService;
    private ScheduledTaskRegistrar taskRegistrar;
    private Map<String, ScheduledFuture<?>> taskFutures = new ConcurrentHashMap<>();

    /**
     * 这个方法在Spring初始化的时候会帮我们执行，这里也会拉取数据库内需要执行的任务，进行添加到定时器里。
     *
     * @param scheduledTaskRegistrar 调度任务注册器
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        Map<String, TriggerTask> taskMap = new HashMap<>();
        // 查询出来当前数据库中存储的所有有效的任务
        List<ConvTunnel> queueTunnels = tunnelService.list(new LambdaQueryWrapper<ConvTunnel>()
                .eq(ConvTunnel::getConvergeMethod, TunnelCMEnum.ACTIVE_INTERFACE_MODE.getCode())
                .notIn(ConvTunnel::getStatus, TunnelStatusEnum.PAUSE.getValue(), TunnelStatusEnum.ABANDON.getValue())
                .ne(ConvTunnel::getDelFlag, 1));
        log.info("查询到的接口采集的管道列表：{}", queueTunnels.stream().map(ConvTunnel::getId).collect(Collectors.toList()));
        // 循环添加任务
        queueTunnels.forEach(t -> {
            TriggerTask triggerTask = createTriggerTask(t);
            taskMap.put(t.getId().toString(), triggerTask);
        });
        // 将任务列表注册到定时器
        scheduledTaskRegistrar.setTriggerTasksList(new ArrayList<>(taskMap.values()));
        this.taskRegistrar = scheduledTaskRegistrar;

        TaskScheduler scheduler = new ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor());
        taskRegistrar.setScheduler(scheduler);

        for (Map.Entry<String, TriggerTask> task : taskMap.entrySet()) {
            TriggerTask triggerTask = task.getValue();
            ScheduledFuture<?> future = scheduler.schedule(triggerTask.getRunnable(), triggerTask.getTrigger());
            assert future != null;
            future.cancel(true);
            taskFutures.put(task.getKey(), future);
        }
    }

    public TriggerTask createTriggerTask(ConvTunnel tunnel) {
        return new TriggerTask(() -> {
            log.info("---------执行管道[{}]的任务！-------", tunnel.getId());
            // 任务执行
            try {
                //activeInterfaceService.activeInterfaceExec(tunnel.getId());
                log.info("执行定时任务：时间：{}， 任务：{}", DateUtil.now(), tunnel.getId());
            } catch (Exception e) {

            }
        }, triggerContext -> {
            log.info("开始执行Cron: {}, tunnelId =  {}", tunnel.getCronStr(), tunnel.getId());
            return new CronTrigger(tunnel.getCronStr()).nextExecutionTime(triggerContext);
        });
    }

    /**
     * 添加任务
     *
     * @param tunnelId    管道id
     * @param triggerTask 调度任务
     */
    public void addTask(String tunnelId, TriggerTask triggerTask) {

        // 如果定时任务id已存在，则取消原定时器，从新创建新定时器，这里也是个更新定时任务的过程。
        if (taskFutures.containsKey(tunnelId)) {
            log.info("the tunnelId [{}] 取消，重新添加", tunnelId);
            cancelTriggerTask(tunnelId);
        }
        TaskScheduler scheduler = taskRegistrar.getScheduler();
        assert scheduler != null;

        ScheduledFuture<?> future = scheduler.schedule(triggerTask.getRunnable(), triggerTask.getTrigger());
        taskFutures.put(tunnelId, future);
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
    }

    public Set<String> getTaskFutures() {
        return taskFutures.keySet();
    }
}
