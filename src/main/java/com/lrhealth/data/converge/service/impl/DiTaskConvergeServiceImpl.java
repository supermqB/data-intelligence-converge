package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.common.enums.conv.CollectDataTypeEnum;
import com.lrhealth.data.common.enums.conv.XdsStatusEnum;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.util.file.LargeFileUtil;
import com.lrhealth.data.converge.dao.adpter.JDBCRepository;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.dao.service.XdsService;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTask;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultView;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskResultViewService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskService;
import com.lrhealth.data.converge.scheduled.thread.AsyncFactory;
import com.lrhealth.data.converge.service.DiTaskConvergeService;
import com.lrhealth.data.converge.service.KafkaService;
import com.lrhealth.data.converge.service.OdsModelService;
import com.lrhealth.data.model.original.model.OriginalModelColumn;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
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

    private static ConcurrentMap<Integer, ConvTaskResultView> dataSaveHandleMap = new ConcurrentHashMap<>();
    @Resource
    private ConvTaskResultViewService taskResultViewService;
    @Resource
    private XdsService xdsService;
    @Resource
    private ConvTaskService taskService;
    @Resource
    private JDBCRepository jdbcRepository;
    @Resource
    private Executor dataSaveThreadPool;
    @Resource
    private OdsModelService odsModelService;
    @Resource
    private LargeFileUtil largeFileUtil;
    @Resource
    private KafkaService kafkaService;

    @Scheduled(cron = "${lrhealth.converge.dataSaveCron}")
    @Override
    public void fileParseAndSave() {
        // 先更新task表, 返回待处理任务实例
        List<ConvTaskResultView> taskResultViewList = generateTaskAndResultView();
        if (CollUtil.isEmpty(taskResultViewList)){
            return;
        }
        // 多线程处理任务实例
        taskResultViewList.forEach(this::dataSave);
    }


    public void dataSave(ConvTaskResultView taskResultView){
        ConvTask convTask = taskService.getById(taskResultView.getTaskId());
        if (dataSaveHandleMap.containsKey(taskResultView.getId())){
            return;
        }

        dataSaveThreadPool.execute(() -> {
            try {
                dataSaveHandleMap.put(taskResultView.getId(), taskResultView);
                AsyncFactory.convTaskLog(convTask.getId(), "开始[" + taskResultView.getTableName() + "]表的入库流程！");
                xdsFileSave(taskResultView, convTask);
                log.info("data save success, taskResultViewId: {}", taskResultView.getId());
            } catch (Exception e) {
                AsyncFactory.convTaskLog(convTask.getId(), "(dataSave)log error, " + ExceptionUtils.getStackTrace(e));
                taskResultViewService.updateById(ConvTaskResultView.builder().id(taskResultView.getId()).status(4).build());
                throw new CommonException("数据入库失败, message: {}", ExceptionUtils.getStackTrace(e));
            }finally {
                dataSaveHandleMap.remove(taskResultView.getId());
            }
        });
    }

    @Override
    public Map<Integer, List<ConvTaskResultView>> getDataSaveMap() {
        return dataSaveHandleMap.values().stream().collect(Collectors.groupingBy(ConvTaskResultView::getTaskId));

    }

    @Override
    public void clearDataSaveMap() {
        dataSaveHandleMap.clear();
    }

    /**
     * 查看是否有所有任务实例都落库完成的task,有则更新状态
     * @return 返回目前状态为downloaded的新增taskResultView数据
     */
    private List<ConvTaskResultView> generateTaskAndResultView(){
        List<ConvTaskResultView> taskResultViewList = taskResultViewService.list();
        if (CollUtil.isEmpty(taskResultViewList)){
            return new ArrayList<>();
        }
        Map<Integer, List<ConvTaskResultView>> listMap = taskResultViewList.stream().collect(Collectors.groupingBy(ConvTaskResultView::getTaskId));
        List<ConvTask> convTaskList = taskService.list(new LambdaQueryWrapper<ConvTask>().eq(ConvTask::getStatus, 4));
        if (CollUtil.isEmpty(convTaskList)){
            return new ArrayList<>();
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
        List<ConvTaskResultView> newResultViewList = new ArrayList<>();
        downloadedTaskResultViewList.forEach(taskResultView -> {
            if (!dataSaveHandleMap.containsKey(taskResultView.getId())){
                newResultViewList.add(taskResultView);
            }
        });
        return newResultViewList;
    }



    private void xdsFileSave(ConvTaskResultView taskResultView, ConvTask convTask){
        // 创建xds
        Xds xds = createXds(taskResultView, convTask);
        long dataSize;
        // 数据落库，获得数据容量
        dataSize = fileDataHandle(xds, convTask.getId(), taskResultView);
        // 更新xds
        updateXds(xds.getId(), dataSize);
        AsyncFactory.convTaskLog(convTask.getId(), "[" + taskResultView.getTableName() + "]表入库成功！");

        // 发送kafka
         kafkaService.xdsSendKafka(xds);
    }



    private long fileDataHandle(Xds xds, Integer taskId, ConvTaskResultView taskResultView){
        List<OriginalModelColumn> originalModelColumns = odsModelService.getcolumnList(xds.getOdsModelName(), xds.getSysCode());
        long startTime = System.currentTimeMillis();
        // 表是否存在的判断
        synchronized (this) {
            if (!checkTableExists(xds.getOdsTableName())) {
                // 创建表
                String tableSql = createTableSql(originalModelColumns, xds.getOdsTableName());
                log.info("table [{}]  sql:{}", xds.getOdsTableName(), tableSql);
                jdbcRepository.execSql(tableSql);
                AsyncFactory.convTaskLog(taskId,  "[" + xds.getOdsTableName() + "]表不存在，创建一个新表");
            }
        }
        // 过滤original_model_column里面的name_en重复数据
        List<OriginalModelColumn> filterModelColumns = new ArrayList<>(originalModelColumns.stream().collect(Collectors.toMap(OriginalModelColumn::getNameEn, column -> column, (column1, column2) -> column1))
                .values());
        Map<String, String> fieldTypeMap = filterModelColumns.stream().collect(Collectors.toMap(OriginalModelColumn::getNameEn, OriginalModelColumn::getFieldType));
        Integer countNumber = largeFileUtil.fileParseAndSave(xds.getStoredFilePath(), xds.getId(), xds.getOdsTableName(), fieldTypeMap, taskId);
        // 获得数据的大概存储大小
        AsyncFactory.convTaskLog(taskId, "数据入库完成，时间：[" + (System.currentTimeMillis() - startTime)
                + "]， 条数：[" + countNumber + "]");
        String avgRowLength = getAvgRowLength(xds.getOdsTableName());
        taskResultViewService.updateById(ConvTaskResultView.builder()
                .id(taskResultView.getId()).status(5).storedTime(LocalDateTime.now())
                .dataItemCount(countNumber)
                .build());

        return countNumber * Long.parseLong(avgRowLength);
    }


    private Xds createXds(ConvTaskResultView taskResultView, ConvTask convTask){
        String dataType = odsModelService.getTableDataType(taskResultView.getTableName(), convTask.getSysCode());
        if (CharSequenceUtil.isBlank(dataType)){
            dataType = CollectDataTypeEnum.BUSINESS.getCode();
        }
        Xds xds =  Xds.builder()
                .id(IdUtil.getSnowflakeNextId())
                .orgCode(convTask.getOrgCode())
                .sysCode(convTask.getSysCode())
                .convergeMethod(convTask.getConvergeMethod())
                .dataType(dataType)
                .dataConvergeStartTime(convTask.getStartTime())
                .dataConvergeStatus(XdsStatusEnum.INIT.getCode())
                .odsModelName(taskResultView.getTableName())
                .oriFileName(taskResultView.getFeStoredFilename())
                .storedFilePath(taskResultView.getStoredPath())
                .storedFileName(taskResultView.getFeStoredFilename())
                .storedFileType("csv")
                .storedFileMode(0)
                .odsTableName(convTask.getSysCode() + "_" + taskResultView.getTableName())
                .storedFileSize(BigDecimal.valueOf(taskResultView.getDataSize()))
                .dataCount(taskResultView.getDataItemCount())
                .createTime(LocalDateTime.now())
                .build();
        xdsService.save(xds);
        return xdsService.getById(xds.getId());
    }

    private void updateXds(Long xdsId, Long dataSize){
        Xds updateXds = Xds.builder().id(xdsId).dataSize(dataSize)
                .dataConvergeEndTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .dataConvergeStatus(1).build();
        xdsService.updateById(updateXds);
    }

    private String getAvgRowLength(String odsTableName){
        // 刷新tables表的数据
        String refreshSql = "ANALYZE TABLE " + odsTableName + " COMPUTE STATISTICS FOR ALL COLUMNS SIZE AUTO;";
        jdbcRepository.execSql(refreshSql);
        // 获取每行的平均大小
        String selectSql = "select AVG_ROW_LENGTH from information_schema.TABLES where TABLE_NAME = '" + odsTableName + "';";
        return jdbcRepository.execSql(selectSql);
    }

     private boolean checkTableExists(String odsTableName){
        String checkSql = "select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '" + odsTableName + "';";
         String result = jdbcRepository.execSql(checkSql);
         return (result != null);
     }


     private String createTableSql(List<OriginalModelColumn> originalModelColumns, String odsTableName){
        StringBuilder createSql = new StringBuilder("CREATE TABLE " + odsTableName + " (\n");
        StringBuilder columnSql = new StringBuilder();
        for (OriginalModelColumn modelColumn : originalModelColumns){
            // 字段名称
            columnSql.append(modelColumn.getNameEn()).append(" ");
            // 字段类型
            if (modelColumn.getFieldTypeLength() != null) {
                columnSql.append(modelColumn.getFieldType()).append("(")
                        .append(modelColumn.getFieldTypeLength()).append(") ");
            } else if (modelColumn.getFieldType().equals("varchar")){
                columnSql.append(modelColumn.getFieldType()).append("(")
                        .append(255).append(") ");
            }else {
                columnSql.append(modelColumn.getFieldType()).append(" ");
            }
//            if (CharSequenceUtil.isNotBlank(modelColumn.getRequiredFlag()) && modelColumn.getRequiredFlag().equals("1")){
//                columnSql.append("NOT NULL,").append("\n");
//            }else {
//                columnSql.append("DEFAULT NULL,").append("\n");
//            }
            columnSql.append("DEFAULT NULL,").append("\n");
        }
        //xds_id和row_id
        columnSql.append("xds_id bigint(20) NOT NULL,").append("\n");
        columnSql.append("row_id bigint(20) NOT NULL AUTO_INCREMENT,").append("\n");
        columnSql.append("KEY ").append(odsTableName).append("_idx1 ").append("(row_id) LOCAL").append("\n");
        createSql.append(columnSql).append(") ")
                .append("AUTO_INCREMENT = 0 AUTO_INCREMENT_MODE = 'ORDER' DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC ")
                .append("COMPRESSION = 'zstd_1.3.8' REPLICA_NUM = 3 BLOCK_SIZE = 16384 USE_BLOOM_FILTER = FALSE TABLET_SIZE = 134217728 PCTFREE = 0;");
        return createSql.toString();
     }



}
