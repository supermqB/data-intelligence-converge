package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.config.ConvergeConfig;
import com.lrhealth.data.converge.common.enums.TaskStatusEnum;
import com.lrhealth.data.converge.dao.entity.*;
import com.lrhealth.data.converge.dao.service.*;
import com.lrhealth.data.converge.model.FileTask;
import com.lrhealth.data.converge.model.TaskFileConfig;
import com.lrhealth.data.converge.model.dto.DsKafkaDto;
import com.lrhealth.data.converge.service.ConvergeService;
import com.lrhealth.data.converge.service.KafkaService;
import com.lrhealth.data.converge.service.XdsInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhaohui
 * @version 1.0
 */
@Service
@Slf4j
public class ConvergeServiceImpl implements ConvergeService {

    @Resource
    private ConvFeNodeService convFeNodeService;

    @Resource
    private ConvTunnelService convTunnelService;

    @Resource
    private ConvTaskService convTaskService;
    @Resource
    private ConvTaskResultViewService convTaskResultViewService;

    @Resource
    private ConvTaskResultFileService convTaskResultFileService;

    @Resource
    private ConvergeConfig convergeConfig;
    @Resource
    private KafkaService kafkaService;
    @Resource
    private XdsInfoService xdsInfoService;

    @Override
    public void sendDsKafka(ConvTask convTask, ConvTask oldTask, Long tunnelId){
        // 发送ds-kafka消息
        if (ObjectUtil.isNotNull(oldTask) && !oldTask.getStatus().equals(TaskStatusEnum.DONE.getValue()) &&
                convTask.getStatus().equals(TaskStatusEnum.DONE.getValue())){
            List<ConvTaskResultView> resultViews = convTaskResultViewService.list(new LambdaQueryWrapper<ConvTaskResultView>()
                    .eq(ConvTaskResultView::getTaskId, convTask.getId()));
            if (CollUtil.isEmpty(resultViews)){
                return;
            }
            DsKafkaDto kafkaDto = DsKafkaDto.builder()
                    .startParams(buildStartParams(convTask))
                    .taskId(convTask.getId())
                    .tunnelId(tunnelId)
                    .build();

            // 发送ds-kafka给数智
            log.info("sendDsKafka,body={}", JSON.toJSONString(kafkaDto));
            kafkaService.dsSendKafka(kafkaDto);
        }
    }

    /** 生成ds需要的启动参数 startParams
     * ○ xds_id：数据采集生成的XDSID
     * ○ table_list：表名称集合，以逗号隔开
     */
    private HashMap<String, String> buildStartParams(ConvTask convTask) {
        HashMap<String, String> startParams = new HashMap<>();
        LambdaQueryWrapper<Xds> xdsWrapper = new LambdaQueryWrapper<>();
        xdsWrapper.eq(Xds::getConvTaskId, convTask.getId());
        List<Xds> xdsList = xdsInfoService.list(xdsWrapper);
        if(!CollectionUtils.isEmpty(xdsList)){
            startParams.put("xds_id",xdsList.get(0).getXdsId().toString());
            String tableList = xdsList.stream().map(Xds::getOdsTableName).
                            collect(Collectors.joining(","));
            startParams.put("table_list",tableList);
            startParams.put("sys_code",convTask.getSysCode());
        }
        return startParams;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFileStatus(TaskFileConfig taskFileConfig, long costTime) {
        try {
            boolean flag = getTaskStatusFlag(taskFileConfig,costTime);
            if (flag) {
                Integer taskId = taskFileConfig.getConvTask().getId();
                ConvTask convTask = convTaskService.getById(taskId);
                convTask.setStatus(4);
                convTaskService.updateById(convTask);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            // 手动回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
        return true;
    }

    public boolean getTaskStatusFlag(TaskFileConfig taskFileConfig, long costTime) {
        String convergeMethod = taskFileConfig.getConvTask().getConvergeMethod();

        if ("1".equals(convergeMethod)){
            return updateTaskResultViewStatus(taskFileConfig.getTaskResultView(), costTime);
        }

        if ("3".equals(convergeMethod)){
            return updateTaskResultFileStatus(taskFileConfig.getTaskResultFile(), costTime);
        }
        return false;
    }

    private boolean updateTaskResultFileStatus(ConvTaskResultFile taskResultFile, long costTime) {
        taskResultFile.setTransferTime(costTime);
        taskResultFile.setStatus(3);
        convTaskResultFileService.updateById(taskResultFile);
        List<ConvTaskResultFile> taskResultFiles = convTaskResultFileService.list(new LambdaQueryWrapper<ConvTaskResultFile>()
                .eq(ConvTaskResultFile::getTaskId, taskResultFile.getTaskId())
                .ne(ConvTaskResultFile::getId, taskResultFile.getId()));
        boolean flag = true;
        for (ConvTaskResultFile resultFile : taskResultFiles) {
            if (resultFile.getStatus() == 1 || resultFile.getStatus() == 2) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    private boolean updateTaskResultViewStatus(ConvTaskResultView taskResultView, long costTime) {
        taskResultView.setTransferTime(costTime);
        taskResultView.setStatus(3);
        convTaskResultViewService.updateById(taskResultView);
        List<ConvTaskResultView> taskResultViews = convTaskResultViewService.list(new LambdaQueryWrapper<ConvTaskResultView>()
                .eq(ConvTaskResultView::getTaskId, taskResultView.getTaskId())
                .ne(ConvTaskResultView::getId, taskResultView.getId()));
        boolean flag = true;
        for (ConvTaskResultView resultView : taskResultViews) {
            if (resultView.getStatus() == 1 || resultView.getStatus() == 2) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    @Override
    @Transactional
    public void resetStatus(ConvTask convTask, TaskFileConfig taskFileConfig) {
        convTask.setStatus(3);
        convTaskService.updateById(convTask);
        String convergeMethod = convTask.getConvergeMethod();
        if ("1".equals(convergeMethod)){
            ConvTaskResultView taskResultView = taskFileConfig.getTaskResultView();
            taskResultView.setStatus(1);
            convTaskResultViewService.updateById(taskResultView);
        }
        if("3".equals(convergeMethod)){
            ConvTaskResultFile taskResultFile = taskFileConfig.getTaskResultFile();
            taskResultFile.setStatus(1);
            convTaskResultFileService.updateById(taskResultFile);
        }
    }

    public TaskFileConfig getTaskConfig(FileTask fileTask) {
        int taskId = fileTask.getTaskId();
        String fileName = fileTask.getFileName();
        ConvTask convTask = convTaskService.getById(taskId);
        FileTask frontNodeTask = new FileTask(convTask.getFedTaskId(), fileName);
        ConvTunnel tunnel = convTunnelService.getById(convTask.getTunnelId());
        ConvFeNode feNode = convFeNodeService.getById(tunnel.getFrontendId());
        ConvTaskResultView taskResultView = convTaskResultViewService.getOne(new LambdaQueryWrapper<ConvTaskResultView>()
                .eq(ConvTaskResultView::getTaskId, taskId)
                .eq(ConvTaskResultView::getFeStoredFilename, fileName),false);
        ConvTaskResultFile taskResultFile = convTaskResultFileService.getOne(new LambdaQueryWrapper<ConvTaskResultFile>()
                .eq(ConvTaskResultFile::getTaskId, taskId)
                .eq(ConvTaskResultFile::getFeStoredFilename, fileName),false);
        String url = feNode.getIp() + ":" + feNode.getPort() + "/file";
        String destPath = convergeConfig.getOutputPath() + File.separator + fileTask.getTaskId()
                + File.separator + fileTask.getFileName().replace(".","_") + File.separator;

        return new TaskFileConfig(convTask, frontNodeTask, tunnel, feNode, taskResultView, taskResultFile,url,
                destPath);
    }
}
