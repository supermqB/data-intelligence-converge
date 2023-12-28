package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.common.enums.conv.CollectDataTypeEnum;
import com.lrhealth.data.common.enums.conv.XdsStatusEnum;
import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import com.lrhealth.data.converge.common.util.file.LargeFileUtil;
import com.lrhealth.data.converge.common.util.thread.AsyncFactory;
import com.lrhealth.data.converge.dao.entity.ConvTask;
import com.lrhealth.data.converge.dao.entity.ConvTaskResultFile;
import com.lrhealth.data.converge.dao.entity.ConvTaskResultView;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.dao.service.ConvTaskResultFileService;
import com.lrhealth.data.converge.dao.service.ConvTaskResultViewService;
import com.lrhealth.data.converge.dao.service.ConvTaskService;
import com.lrhealth.data.converge.dao.service.XdsService;
import com.lrhealth.data.converge.model.bo.ColumnDbBo;

import com.lrhealth.data.converge.model.dto.DataSourceDto;
import com.lrhealth.data.converge.model.dto.FileMessageDTO;
import com.lrhealth.data.converge.service.*;
import com.lrhealth.data.model.original.model.OriginalModelColumn;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.relational.core.sql.In;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author jinmengyu
 * @date 2023-10-12
 */
@Slf4j
@Service
public class DiTaskConvergeServiceImpl implements DiTaskConvergeService {

    private static ConcurrentMap<String, FileMessageDTO> dataSaveHandleMap = new ConcurrentHashMap<>();
    @Resource
    private ConvTaskResultViewService taskResultViewService;
    @Resource
    private ConvTaskResultFileService taskResultFileService;
    @Resource
    private XdsService xdsService;
    @Resource
    private ConvTaskService taskService;
    @Resource
    private Executor dataSaveThreadPool;
    @Resource
    private OdsModelService odsModelService;
    @Resource
    private LargeFileUtil largeFileUtil;
    @Resource
    private KafkaService kafkaService;
    @Resource
    private DbSqlService dbSqlService;
    @Resource
    private TunnelService tunnelService;

    @Scheduled(cron = "${lrhealth.converge.dataSaveCron}")
    @Override
    public void fileParseAndSave() {
        List<FileMessageDTO> fileInfoList = new ArrayList<>();
        // 先更新task表, 获得待处理实例
        generateTaskAndResultView(fileInfoList);
        generateTaskAndResultFile(fileInfoList);
        // 没有任务存在
        if (CollUtil.isEmpty(fileInfoList)) {
            return;
        }
        // 多线程处理任务实例
        fileInfoList.forEach(fileMessageDTO -> dataSave(fileMessageDTO.getTunnelCMEnum() + "-" + fileMessageDTO.getTaskResultId(), fileMessageDTO));
    }


    @Override
    public void dataSave(String mapKey, FileMessageDTO fileMessageDTO) {
        ConvTask convTask = taskService.getById(fileMessageDTO.getTaskId());

        dataSaveThreadPool.execute(() -> {
            try {
                dataSaveHandleMap.put(mapKey, fileMessageDTO);
                AsyncFactory.convTaskLog(convTask.getId(), "开始[" + fileMessageDTO.getTableName() + "]表的入库流程！");
                xdsFileSave(fileMessageDTO, convTask);
                log.info("data save success, taskResultId: {}", mapKey);
            } catch (Exception e) {
                AsyncFactory.convTaskLog(convTask.getId(), "(dataSave)log error, " + ExceptionUtils.getStackTrace(e));
                if (fileMessageDTO.getTunnelCMEnum().equals(TunnelCMEnum.LIBRARY_TABLE)) {
                    taskResultViewService.updateById(ConvTaskResultView.builder().id(fileMessageDTO.getTaskResultId()).status(4).build());
                } else {
                    taskResultFileService.updateById(ConvTaskResultFile.builder().id(fileMessageDTO.getTaskResultId()).status(4).build());

                }
            } finally {
                dataSaveHandleMap.remove(mapKey);
            }
        });
    }

    @Override
    public Map<Integer, List<FileMessageDTO>> getDataSaveMap() {
        return dataSaveHandleMap.values().stream().collect(Collectors.groupingBy(FileMessageDTO::getTaskId));

    }

    @Override
    public void clearDataSaveMap() {
        dataSaveHandleMap.clear();
    }

    /**
     * 查看是否有所有任务实例都落库完成的task,有则更新状态
     *
     * @return 返回目前状态为downloaded的新增taskResultView数据
     */
    private void generateTaskAndResultView(List<FileMessageDTO> fileInfoList) {
        List<ConvTaskResultView> taskResultViewList = taskResultViewService.list();
        if (CollUtil.isEmpty(taskResultViewList)) {
            return;
        }
        Map<Integer, List<ConvTaskResultView>> listMap = taskResultViewList.stream().collect(Collectors.groupingBy(ConvTaskResultView::getTaskId));
        List<ConvTask> convTaskList = taskService.list(new LambdaQueryWrapper<ConvTask>().eq(ConvTask::getStatus, 4));
        if (CollUtil.isEmpty(convTaskList)) {
            return;
        }
        Set<Integer> taskNotDoneSet = convTaskList.stream().map(ConvTask::getId).collect(Collectors.toSet());
        Set<Integer> finishedTaskList = listMap.entrySet().stream()
                .filter(entry -> entry.getValue().stream().allMatch(taskResultView -> taskResultView.getStatus() == 5))
                .filter(entry -> taskNotDoneSet.contains(entry.getKey()))
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        // 更新状态为done
        finishedTaskList.forEach(taskId -> {
            taskService.updateById(ConvTask.builder().id(taskId).status(5).build());
            log.info("task[{}]更新为已完成!", taskId);
        });

        List<ConvTaskResultView> downloadedTaskResultViewList = taskResultViewList.stream().filter(taskResultView -> taskResultView.getStatus() == 3).collect(Collectors.toList());
        downloadedTaskResultViewList.forEach(taskResultView -> {
            if (!dataSaveHandleMap.containsKey(TunnelCMEnum.LIBRARY_TABLE + "-" + taskResultView.getId())) {
                FileMessageDTO fileMessageDTO = FileMessageDTO.builder()
                        .tunnelCMEnum(TunnelCMEnum.LIBRARY_TABLE)
                        .taskResultId(taskResultView.getId())
                        .feStoredFileName(taskResultView.getFeStoredFilename())
                        .dataItemCount(taskResultView.getDataItemCount())
                        .dataSize(taskResultView.getDataSize())
                        .storedPath(taskResultView.getStoredPath())
                        .tableName(taskResultView.getTableName())
                        .taskId(taskResultView.getTaskId()).build();
                fileInfoList.add(fileMessageDTO);
            }
        });
    }

    private void generateTaskAndResultFile(List<FileMessageDTO> fileInfoList) {
        List<ConvTaskResultFile> taskResultFileList = taskResultFileService.list();
        if (CollUtil.isEmpty(taskResultFileList)) {
            return;
        }
        Map<Integer, List<ConvTaskResultFile>> listMap = taskResultFileList.stream().collect(Collectors.groupingBy(ConvTaskResultFile::getTaskId));
        List<ConvTask> convTaskList = taskService.list(new LambdaQueryWrapper<ConvTask>().eq(ConvTask::getStatus, 4));
        if (CollUtil.isEmpty(convTaskList)) {
            return;
        }
        Set<Integer> taskNotDoneSet = convTaskList.stream().map(ConvTask::getId).collect(Collectors.toSet());
        Set<Integer> finishedTaskList = listMap.entrySet().stream()
                .filter(entry -> entry.getValue().stream().allMatch(taskResultFile -> taskResultFile.getStatus() == 5))
                .filter(entry -> taskNotDoneSet.contains(entry.getKey()))
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        // 更新状态为done
        finishedTaskList.forEach(taskId -> {
            taskService.updateById(ConvTask.builder().id(taskId).status(5).build());
            log.info("task[{}]更新为已完成!", taskId);
        });

        List<ConvTaskResultFile> downloadedTaskResultFileList = taskResultFileList.stream().filter(taskResultFile -> taskResultFile.getStatus() == 3).collect(Collectors.toList());
        downloadedTaskResultFileList.forEach(taskResultFile -> {
            if (!dataSaveHandleMap.containsKey(TunnelCMEnum.FILE_MODE + "-" + taskResultFile.getId())) {
                FileMessageDTO fileMessageDTO = FileMessageDTO.builder()
                        .tunnelCMEnum(TunnelCMEnum.FILE_MODE)
                        .taskResultId(taskResultFile.getId())
                        .feStoredFileName(taskResultFile.getFeStoredFilename())
                        .dataSize(taskResultFile.getDataSize())
                        .storedPath(taskResultFile.getStoredPath())
                        .tableName(taskResultFile.getTableName())
                        .taskId(taskResultFile.getTaskId()).build();
                fileInfoList.add(fileMessageDTO);
            }
        });
    }


    private void xdsFileSave(FileMessageDTO fileMessageDTO, ConvTask convTask) {
        // 创建xds
        Xds xds = createXds(fileMessageDTO, convTask);

        DataSourceDto dataSourceDto = tunnelService.getWriterDataSourceByTunnel(convTask.getTunnelId());
        // 数据落库，获得数据条数
        Integer countNumber = fileDataHandle(xds, convTask.getId(), dataSourceDto);

        // 更新任务实例信息
        updateTaskResult(fileMessageDTO.getTunnelCMEnum(), fileMessageDTO.getTaskResultId(), countNumber);

        // 更新xds
        updateXds(xds.getId(), xds.getOdsTableName(), countNumber, dataSourceDto, xds.getOdsModelName());
        AsyncFactory.convTaskLog(convTask.getId(), "[" + fileMessageDTO.getTableName() + "]表入库成功！");

        // 发送kafka
        kafkaService.xdsSendKafka(xds);
    }

    private void updateTaskResult(TunnelCMEnum tunnelCMEnum, Integer taskResultId, Integer countNumber) {
        if (tunnelCMEnum.equals(TunnelCMEnum.LIBRARY_TABLE)) {
            taskResultViewService.updateById(ConvTaskResultView.builder()
                    .id(taskResultId).status(5).storedTime(LocalDateTime.now())
                    .dataItemCount(countNumber)
                    .build());
        }
        taskResultFileService.updateById(ConvTaskResultFile.builder()
                .id(taskResultId).status(5).storedTime(LocalDateTime.now())
                .dataItemCount(countNumber)
                .build());
    }


    /**
     * 数据落库，获得入库条数
     * 汇聚端文件删除
     *
     * @param xds
     * @param taskId
     * @return
     */
    private Integer fileDataHandle(Xds xds, Integer taskId, DataSourceDto dataSourceDto) {
        List<OriginalModelColumn> originalModelColumns = odsModelService.getcolumnList(xds.getOdsModelName(), xds.getSysCode());
        // 数据写入
        Integer countNumber = dataTableSave(xds, taskId, originalModelColumns, dataSourceDto);

        // todo: 配置化
        try {
            // 删除汇聚端的文件
            Files.delete(Paths.get(xds.getStoredFilePath()));
        } catch (IOException e) {
            log.error("delete converge file error: {}", ExceptionUtils.getStackTrace(e));
        }
        return countNumber;

    }


    private Integer dataTableSave(Xds xds, Integer taskId, List<OriginalModelColumn> originalModelColumns, DataSourceDto dataSourceDto) {
        long startTime = System.currentTimeMillis();
        // 表是否存在的判断
        synchronized (this) {
            if (!dbSqlService.checkOdsTableExist(xds.getOdsTableName(), dataSourceDto)) {
                // 创建表
                List<ColumnDbBo> collect = originalModelColumns.stream().map(columnInfo -> ColumnDbBo.builder().columnName(columnInfo.getNameEn()).fieldType(columnInfo.getFieldType()).fieldLength(columnInfo.getFieldTypeLength()).build()).collect(Collectors.toList());
                dbSqlService.createTable(collect, xds.getOdsTableName(), dataSourceDto);
                AsyncFactory.convTaskLog(taskId, "[" + xds.getOdsTableName() + "]表不存在，创建一个新表");
            }
        }
        // 过滤original_model_column里面的name_en重复数据
        List<OriginalModelColumn> filterModelColumns = new ArrayList<>(originalModelColumns.stream().collect(Collectors.toMap(OriginalModelColumn::getNameEn, column -> column, (column1, column2) -> column1))
                .values());
        Map<String, String> fieldTypeMap = filterModelColumns.stream().collect(Collectors.toMap(OriginalModelColumn::getNameEn, OriginalModelColumn::getFieldType));
        Integer countNumber = largeFileUtil.fileParseAndSave(xds.getStoredFilePath(), xds.getId(), xds.getOdsTableName(), fieldTypeMap, taskId, dataSourceDto);
        // 获得数据的大概存储大小
        AsyncFactory.convTaskLog(taskId, "数据入库完成，时间：[" + (System.currentTimeMillis() - startTime)
                + "]， 条数：[" + countNumber + "]");
        return countNumber;
    }


    private Xds createXds(FileMessageDTO fileMessageDto, ConvTask convTask) {
        String dataType = odsModelService.getTableDataType(fileMessageDto.getTableName(), convTask.getSysCode());
        if (CharSequenceUtil.isBlank(dataType)) {
            dataType = CollectDataTypeEnum.BUSINESS.getCode();
        }
        Xds xds = Xds.builder()
                .id(IdUtil.getSnowflakeNextId())
                .orgCode(convTask.getOrgCode())
                .sysCode(convTask.getSysCode())
                .convergeMethod(convTask.getConvergeMethod())
                .dataType(dataType)
                .dataConvergeStartTime(convTask.getStartTime())
                .dataConvergeStatus(XdsStatusEnum.INIT.getCode())
                .odsModelName(fileMessageDto.getTableName())
                .oriFileName(fileMessageDto.getFeStoredFileName())
                .storedFilePath(fileMessageDto.getStoredPath())
                .storedFileName(fileMessageDto.getFeStoredFileName())
                .storedFileType("csv")
                .storedFileMode(0)
                .odsTableName(convTask.getSysCode() + "_" + fileMessageDto.getTableName())
                .storedFileSize(BigDecimal.valueOf(fileMessageDto.getDataSize()))
                .dataCount(fileMessageDto.getDataItemCount())
                .createTime(LocalDateTime.now())
                .build();
        xdsService.save(xds);
        return xdsService.getById(xds.getId());
    }

    private void updateXds(Long xdsId, String odsTableName, Integer countNumber, DataSourceDto dataSourceDto, String odsModelName) {
        Integer avgRowLength = dbSqlService.getAvgRowLength(odsTableName, dataSourceDto,odsModelName);
        // 文件中的数据写入后消耗的数据库容量
        long dataSize = countNumber * avgRowLength;
        Xds updateXds = Xds.builder().id(xdsId).dataSize(dataSize)
                .dataConvergeEndTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .dataConvergeStatus(1).build();
        xdsService.updateById(updateXds);
    }
}
