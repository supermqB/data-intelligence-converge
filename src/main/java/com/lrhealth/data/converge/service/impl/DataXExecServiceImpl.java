package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.datax.core.Engine;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.enums.TaskStatusEnum;
import com.lrhealth.data.converge.common.enums.TunnelConnectStatusEnum;
import com.lrhealth.data.converge.common.util.QueryParserUtil;
import com.lrhealth.data.converge.common.util.TemplateMakerUtil;
import com.lrhealth.data.converge.common.util.thread.AsyncFactory;
import com.lrhealth.data.converge.dao.entity.ConvDataXJob;
import com.lrhealth.data.converge.dao.entity.ConvTaskResultView;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.service.ConvDataXJobService;
import com.lrhealth.data.converge.dao.service.ConvTaskResultViewService;
import com.lrhealth.data.converge.dao.service.ConvTaskService;
import com.lrhealth.data.converge.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.datax.plugin.reader.AbstractReader;
import com.lrhealth.data.converge.datax.plugin.reader.ReaderFactory;
import com.lrhealth.data.converge.model.dto.TableInfoDto;
import com.lrhealth.data.converge.service.DataXExecService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author jinmengyu
 * @date 2023-09-12
 */
@Service
@Slf4j
public class DataXExecServiceImpl implements DataXExecService {
    @Resource
    private ConvDataXJobService jobService;
    @Resource
    private ConvTaskService taskService;
    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private ConvTaskResultViewService resultViewService;
    @Resource
    private Executor dataxThreadPool;
    @Value("${datax.collect-multith}")
    private String dataxCollectMultith;
    @Value("${datax.execute-number-limit}")
    private Integer limitNumber;
    @Value("${datax.home}")
    private String dataxHome;
    @Value("${datax.json-path}")
    private String dataxJsonPath;
    @Value("${datax.dest-path}")
    private String fileDestPath;
    @Value("${spring.datasource.rdcp-ext.jdbcUrl}")
    private String jdbcUrl;
    @Value("${spring.datasource.rdcp-ext.username}")
    private String username;
    @Value("${spring.datasource.rdcp-ext.password}")
    private String password;
    public static ConcurrentMap<Integer, CountDownLatch> DOWN_LATCH_MAP = new ConcurrentHashMap<>();

    @Override
    public void run(Long tunnelId, Integer taskId, Integer execStatus, Integer oldTaskId) throws InterruptedException {
        ConvTunnel tunnel = tunnelService.getById(tunnelId);

        // 创建或更新dataXJob
        List<ConvDataXJob> jobList = jobService.list(new LambdaQueryWrapper<ConvDataXJob>().eq(ConvDataXJob::getTunnelId, tunnel.getId()));

        DOWN_LATCH_MAP.put(taskId, new CountDownLatch(jobList.size()));
        long dataxStart = System.currentTimeMillis();
        jobList.forEach(dataXJob -> {
            if ("0".equals(dataxCollectMultith)) {
                // 多线程执行
                dataxThreadPool.execute(() -> {
                    try {
                        dataXJobCollect(tunnel, taskId, dataXJob, execStatus, oldTaskId);
                    }catch (Exception e){
                        log.error("dataXJobCollect error,taskId={},{}",taskId, ExceptionUtils.getStackTrace(e));
                    }finally {
                        log.info("未处理数据条数:{}", DOWN_LATCH_MAP.get(taskId).getCount());
                        DOWN_LATCH_MAP.get(taskId).countDown();
                    }
                });
            }else {
                try {
                    dataXJobCollect(tunnel, taskId, dataXJob, execStatus, oldTaskId);
                }catch (Exception e){
                    log.error("dataXJobCollect error,taskId={},{}",taskId, ExceptionUtils.getStackTrace(e));
                }finally {
                    log.info("未处理数据条数:{}", DOWN_LATCH_MAP.get(taskId).getCount());
                    DOWN_LATCH_MAP.get(taskId).countDown();
                }
            }
        });
        DOWN_LATCH_MAP.get(taskId).await();
        log.info("datax cost time(step1)={},taskId={}", System.currentTimeMillis() - dataxStart, taskId);

        // 更新文件大小
        updateFileSize(taskId);
    }



    private void dataXJobCollect(ConvTunnel tunnel, Integer taskId, ConvDataXJob dataXJob, Integer execStatus, Integer oldTaskId){
        // 获得并判断开始和结束索引
        AbstractReader reader = new ReaderFactory().getReader(QueryParserUtil.getDbType(tunnel.getJdbcUrl()));
        log.info("开始生成管道{}中{}的DataX执行文件", tunnel.getId(), dataXJob.getTableName());
        String endIndex = null;
        String startIndex = null;
        // 直接调度，新任务，读取开始和结束索引
        if (execStatus == 0 && dataXJob.getSeqFields() != null) {
            endIndex = reader.readerTableSeqFieldIndex(tunnel, dataXJob.getTableName(), dataXJob.getSeqFields(), "endIndex");
            startIndex = getStartIndex(tunnel, dataXJob, reader);
            if (endIndex.equals(startIndex)) {
                log.info("开始索引和结束索引相同，{}表没有新增数据", dataXJob.getTableName());
                return;
            }
            if (Long.parseLong(endIndex) - Long.parseLong(startIndex) > limitNumber && limitNumber != 0) {
                endIndex = String.valueOf(Long.parseLong(startIndex) + limitNumber);
            }
        } else if (execStatus == 1) {
            // 重新调度，使用上一次任务的开始和结束索引
            ConvTaskResultView existedInstance = resultViewService.getOne(new LambdaQueryWrapper<ConvTaskResultView>()
                    .eq(ConvTaskResultView::getTaskId, oldTaskId)
                    .eq(ConvTaskResultView::getDataxJobId, dataXJob.getId()));
            endIndex = String.valueOf(existedInstance.getEndIndex());
            startIndex = String.valueOf(existedInstance.getStartIndex());
        }
        // 创建job运行实例
        ConvTaskResultView jobExecInstance = resultViewService.createJobExecInstance(taskId, dataXJob.getId(), dataXJob.getTableName(), startIndex, endIndex);
        log.info("生成job运行实例, id:{}, status:{}",jobExecInstance.getId(), jobExecInstance.getStatus());
        // 生成datax执行文件
        generateDataXJobJson(reader, dataXJob, tunnel, jobExecInstance, startIndex, endIndex);
        // 更新task, datax采集
        taskService.updateTaskStatus(taskId, TaskStatusEnum.EXTRACTING);

        Long jobId = resultViewService.getDataXJoId(jobExecInstance.getId(), taskId);

        AsyncFactory.convTaskLog(taskId, dataXJob.getTableName() + " collect job start");
        dataXExec(dataXJob.getJsonPath(), jobId, dataXJob.getJobMode(), taskId);
        AsyncFactory.convTaskLog(taskId, dataXJob.getTableName() + "collect job end");

        taskService.updateTaskStatus(taskId, TaskStatusEnum.DUMPING);
    }

    @Override
    public void dataXConfig(ConvTunnel tunnel, List<TableInfoDto> tableInfoDtoList) {
        try {
            // 废弃删除的表
            Set<String> existedTableSet = tableInfoDtoList.stream().map(TableInfoDto::getTableName).collect(Collectors.toSet());
            List<ConvDataXJob> dataXJobs = jobService.list(new LambdaQueryWrapper<ConvDataXJob>().eq(ConvDataXJob::getTunnelId, tunnel.getId()));
            List<ConvDataXJob> deleteTable = dataXJobs.stream().filter(dataXJob -> !existedTableSet.contains(dataXJob.getTableName())).collect(Collectors.toList());
            deleteTable.forEach(tableInfo -> jobService.removeById(tableInfo.getId()));
            // 创建或更新dataXJob表
            tableInfoDtoList.forEach(tableInfoDto -> createOrUpdateDataXJsonJob(tunnel.getId(), tableInfoDto));
        } catch (Exception e) {
            log.error("(dataxConfig)log error,{}", ExceptionUtils.getStackTrace(e));
        }
    }


    @Override
    public String dataXExec(String jsonPath, Long jobExecResultId, String mode, Integer taskId) {
        try {
            System.setProperty("datax.home", dataxHome);
            System.setProperty("datax.task.dbType", TunnelConnectStatusEnum.DIRECT.getValue());
            System.setProperty("datax.task.jdbcUrl", jdbcUrl);
            System.setProperty("datax.task.dbUserName", username);
            System.setProperty("datax.task.dbPassword", password);
            String[] dataXArgs2 = {"-job", jsonPath, "-mode", mode, "-jobid", String.valueOf(jobExecResultId)};
            Engine.entry(dataXArgs2);
        } catch (Throwable e) {
            log.error("(dataXExec)log error,{}", ExceptionUtils.getStackTrace(e));
            AsyncFactory.convTaskLog(taskId, ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    private void updateFileSize(Integer taskId){
        List<ConvTaskResultView> jobList = resultViewService.list(new LambdaQueryWrapper<ConvTaskResultView>().eq(ConvTaskResultView::getTaskId, taskId));
        if (ObjectUtil.isNull(jobList)){
            return;
        }
        try {
            Thread.sleep(5000);
        }catch (Exception e){
            log.error("(dataxConfig)log error,{}", ExceptionUtils.getStackTrace(e));
            Thread.currentThread().interrupt();
        }
        jobList.forEach(jobExecInstance -> {
            File taskFile = new File(jobExecInstance.getStoredPath());
            if (taskFile.exists() && taskFile.isFile()){
                resultViewService.updateById(ConvTaskResultView.builder().id(jobExecInstance.getId())
                        .dataSize(taskFile.length()).status(3).build());
                log.info("file: {}, fileSize: {}", taskFile.getName(), taskFile.length());
            } else {
                log.error("文件不存在，resultViewId: {}", jobExecInstance.getId());
            }
        });
    }

    /**
     *  再dataXJob中新建或更新数据，保存sqlQuery
     * @param tunnelId 管道id
     * @param tableInfo 表信息
     */
    private void createOrUpdateDataXJsonJob(Long tunnelId, TableInfoDto tableInfo) {
        // 生成dataX执行文件
        String tableName = tableInfo.getTableName();
        String dataXJsonCatalog = dataxJsonPath + FileUtil.FILE_SEPARATOR + tunnelId;
        File jsonDirectory = new File(dataXJsonCatalog);
        // 新建目录
        if (!jsonDirectory.exists()){
            boolean mkdirs = jsonDirectory.mkdirs();
            if (!mkdirs){
                log.error("管道datax文件路径创建失败");
            }
        }
        // json的完整路径
        String dataXJsonPath = dataxJsonPath + FileUtil.FILE_SEPARATOR + tunnelId + FileUtil.FILE_SEPARATOR + tableName + ".json";
        // 创建或更新数据
        ConvDataXJob dataXJob = jobService.createOrUpdateDataXJob(dataXJsonPath, tunnelId, tableName, tableInfo);
        if (ObjectUtil.isNull(dataXJob)){
            log.error("(tunnelConfig) datax job insert failure, tunnel: {}, table: {}", tunnelId, tableName);
            throw new CommonException("DATAX 执行文件生成失败");
        }else {
            log.info("(tunnelConfig) datax job insert success, tunnel: {}, table: {}", tunnelId, tableName);
        }
    }

    /**
     * 生成dataX执行文件
     * 需求定义前端可能会增加id范围的筛选
     * 每天根据sqlQuery中的条件，如果有beginIndex和endIndex，动态填充，在执行任务前才去生成dataX的执行文件
     * @param dataXJob datax执行信息
     */
    private void generateDataXJobJson(AbstractReader reader, ConvDataXJob dataXJob, ConvTunnel tunnel, ConvTaskResultView jobExecInstance, String startIndex, String endIndex){
        if (jobExecInstance.getStatus() == 1){
            String sqlQuery = updateIndexNumber(dataXJob.getSqlQuery(), startIndex, endIndex);
            log.info("执行的采集语句: {}",sqlQuery);
            // 生成dataX执行文件
            reader.generateDatabaseReader(tunnel, dataXJob.getTableName(), sqlQuery, dataXJob.getJsonPath(), fileDestPath);
        }
        // 检查执行文件
        File file = new File(dataXJob.getJsonPath());
        if (!file.exists()){
            log.error("管道配置生成的datax执行生成错误, tunnelId: {}, tableName: {}", tunnel.getId(), dataXJob.getTableName());
        }
    }

    private String updateIndexNumber(String sqlQuery, String startIndex, String endIndex){
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("start_index", startIndex);
        columnMap.put("end_index", endIndex);
        try {
            return TemplateMakerUtil.process(sqlQuery, columnMap, null);
        }catch (Exception e){
            log.error("log error,{}", ExceptionUtils.getStackTrace(e));
        }
        return sqlQuery;
    }



    private String getStartIndex(ConvTunnel tunnel, ConvDataXJob dataXJob, AbstractReader reader){
        List<ConvTaskResultView> tableJobList = resultViewService.list(new LambdaQueryWrapper<ConvTaskResultView>().eq(ConvTaskResultView::getDataxJobId, dataXJob.getId())
                .eq(ConvTaskResultView::getTableName, dataXJob.getTableName()).orderByDesc(ConvTaskResultView::getId));
        String minNumber = reader.readerTableSeqFieldIndex(tunnel, dataXJob.getTableName(), dataXJob.getSeqFields(), "startIndex");
        if (tableJobList.isEmpty()){
            return minNumber;
        }
        return String.valueOf(tableJobList.get(0).getEndIndex());
    }
}
