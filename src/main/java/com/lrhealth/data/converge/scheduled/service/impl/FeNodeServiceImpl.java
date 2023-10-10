package com.lrhealth.data.converge.scheduled.service.impl;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.scheduled.config.ConvergeConfig;
import com.lrhealth.data.converge.scheduled.config.exception.FeNodeStatusException;
import com.lrhealth.data.converge.scheduled.config.exception.PingException;
import com.lrhealth.data.converge.scheduled.dao.entity.*;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskLogService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskResultViewService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.scheduled.model.FileTask;
import com.lrhealth.data.converge.scheduled.model.dto.*;
import com.lrhealth.data.converge.scheduled.service.FeNodeService;
import com.lrhealth.data.converge.scheduled.utils.RsaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * @author zhaohui
 * @version 1.0
 */
@Service
@Slf4j
public class FeNodeServiceImpl implements FeNodeService {

    @Resource
    private ConvergeConfig convergeConfig;

    @Resource
    private ConvTunnelService convTunnelService;

    @Resource
    private ConvTaskService convTaskService;

    @Resource
    private ConvTaskLogService convTaskLogService;

    @Resource
    private ConvTaskResultViewService convTaskResultViewService;

    @Override
    @Retryable(value = {PingException.class}, backoff = @Backoff(delay = 2000, multiplier = 1.5))
    public boolean ping(String ip, String port) {
        String url = ip + ":" + port + "/health/pong";
        RSA instance = RsaUtils.getInstance(convergeConfig.getPrivateKeyStr());
        String token = "lrhealth:" + System.currentTimeMillis();
        try {
            log.info("ping: " + url);
            String body = HttpRequest.get(url)
                    .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                    .timeout(3000)
                    .execute().body();
            JSONObject jsonObject = JSONObject.parseObject(body);
            return "200".equals(jsonObject.get("code"));
        } catch (Exception e) {
            throw new PingException("ping 前置机异常！" + url + "\n" +e.getMessage() + "\n"
            + Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    @Recover
    public boolean recover(PingException e) {
        return false;
    }


    @Override
    @Retryable(value = {FeNodeStatusException.class}, backoff = @Backoff(delay = 2000, multiplier = 1.5))
    public FrontendStatusDto getFeNodeStatus(ConvFeNode node) {
        String url = node.getIp() + ":" + node.getPort() + "/task/front/status";
        RSA instance = RsaUtils.getInstance(convergeConfig.getPrivateKeyStr());
        String token = "lrhealth:" + System.currentTimeMillis();
        try {
            String result = HttpRequest.get(url)
                    .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                    .timeout(3000)
                    .execute().body();
            Object value = JSONObject.parseObject(result).get("value");
            return JSONObject.parseObject(JSONObject.toJSONString(value),
                    FrontendStatusDto.class);
        } catch (Exception e) {
            throw new FeNodeStatusException("获取前置机：" + node.getIp() + "状态异常！\n" + e.getMessage()
            + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    @Recover
    public FrontendStatusDto recover(FeNodeStatusException e) {
        return null;
    }

    @Override
    @Transactional
    public ConvTunnel updateTunnel(TunnelStatusDto tunnelStatusDto) {
        Long tunnelId = tunnelStatusDto.getTunnelId();
        ConvTunnel tunnel = convTunnelService.getById(tunnelId);
        if (tunnel == null){
            log.info("不存在的tunnel: " + tunnelId);
            return null;
        }
        tunnel.setStatus(tunnelStatusDto.getTunnelStatus());
        convTunnelService.updateById(tunnel);
        return tunnel;
    }

    @Override
    @Transactional
    public ConvTask saveOrUpdateTask(TaskStatusDto taskStatusDto, ConvTunnel tunnel) {
        ConvTask convTask = new ConvTask();
        ConvTask one = convTaskService.getOne(new LambdaQueryWrapper<ConvTask>()
                .eq(ConvTask::getFedTaskId, taskStatusDto.getTaskId())
                .eq(ConvTask::getTunnelId, tunnel.getId()), false);
        if (one != null && one.getStatus() > 3) {
            return one;
        }
        BeanUtils.copyProperties(taskStatusDto, convTask);
        if (one != null){
            convTask.setId(one.getId());
        }
        convTask.setFedTaskId(taskStatusDto.getTaskId());
        convTask.setTunnelId(tunnel.getId());
        convTask.setName(tunnel.getName() + "_任务" + taskStatusDto.getTaskId());
        convTask.setSysCode(tunnel.getSysCode());
        convTask.setOrgCode(tunnel.getOrgCode());
        convTask.setConvergeMethod(tunnel.getConvergeMethod());
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        convTask.setStartTime(LocalDateTime.parse(taskStatusDto.getStartTime(), df));
        if (taskStatusDto.getEndTime() != null) {
            convTask.setEndTime(LocalDateTime.parse(taskStatusDto.getEndTime(), df));
        }
        convTask.setDelFlag(0);
        convTaskService.saveOrUpdate(convTask, new LambdaQueryWrapper<ConvTask>()
                .eq(ConvTask::getTunnelId, tunnel.getId())
                .eq(ConvTask::getFedTaskId, taskStatusDto.getTaskId()));
        return convTask;
    }

    @Override
    @Transactional
    public void saveOrUpdateLog(TaskLogDto taskLog, ConvTask convTask) {
        ConvTaskLog convTaskLog = new ConvTaskLog();
        convTaskLog.setTaskId(convTask.getId());
        convTaskLog.setFedLogId(taskLog.getLogId());
        convTaskLog.setLogDetail(taskLog.getLogDetail());
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        convTaskLog.setTimestamp(LocalDateTime.parse(taskLog.getLogTime(), df));
        convTaskLogService.saveOrUpdate(convTaskLog, new LambdaQueryWrapper<ConvTaskLog>()
                .eq(ConvTaskLog::getFedLogId, taskLog.getLogId())
                .eq(ConvTaskLog::getTaskId, convTask.getId()));
    }

    @Override
    @Transactional
    public ConvTaskResultView saveOrUpdateFile(ResultViewInfoDto resultViewInfoDto, ConvTask convTask) {
        ConvTaskResultView convTaskResultView = new ConvTaskResultView();
        ConvTaskResultView taskResultView = convTaskResultViewService.getOne(new LambdaQueryWrapper<ConvTaskResultView>()
                .eq(ConvTaskResultView::getTaskId, convTask.getId())
                .eq(ConvTaskResultView::getTableName,resultViewInfoDto.getTableName()), false);
        if (taskResultView != null  && taskResultView.getStatus() > 1){
            return taskResultView;
        }
        BeanUtils.copyProperties(resultViewInfoDto, convTaskResultView);
        convTaskResultView.setTaskId(convTask.getId());
        convTaskResultView.setDataItemCount(resultViewInfoDto.getRecordCount());
        convTaskResultView.setFeStoredPath(resultViewInfoDto.getFilePath());
        String destPath = convergeConfig.getOutputPath() + File.separator + convTask.getId()
                + File.separator + resultViewInfoDto.getFileName().replace(".","_") + File.separator;
        convTaskResultView.setStoredPath(destPath + resultViewInfoDto.getFileName());
        convTaskResultView.setFeStoredFilename(resultViewInfoDto.getFileName());
        convTaskResultView.setDataSize(resultViewInfoDto.getFileSize());
        convTaskResultView.setDelFlag(0);

        convTaskResultViewService.saveOrUpdate(convTaskResultView,new LambdaQueryWrapper<ConvTaskResultView>()
                .eq(ConvTaskResultView::getTaskId, convTask.getId())
                .eq(ConvTaskResultView::getTableName, resultViewInfoDto.getTableName()));
        return convTaskResultView;
    }
}
