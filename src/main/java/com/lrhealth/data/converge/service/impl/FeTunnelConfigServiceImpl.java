package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.lrhealth.data.converge.common.enums.FileStorageTypeEnum;
import com.lrhealth.data.converge.common.enums.LibraryTableModelEnum;
import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import com.lrhealth.data.converge.common.enums.TunnelColTypeEnum;
import com.lrhealth.data.converge.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.*;
import com.lrhealth.data.converge.dao.service.*;
import com.lrhealth.data.converge.model.dto.*;
import com.lrhealth.data.converge.scheduled.DownloadFileTask;
import com.lrhealth.data.converge.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.lrhealth.data.converge.common.enums.TunnelCMEnum.CDC_LOG;
import static com.lrhealth.data.converge.common.enums.TunnelCMEnum.LIBRARY_TABLE;

/**
 * @author jinmengyu
 * @date 2023-11-14
 */
@Slf4j
@Service
public class FeTunnelConfigServiceImpl implements FeTunnelConfigService {

    @Resource
    private ConvTunnelService tunnelService;

    @Resource
    private ConvCollectFieldService collectFieldService;
    @Resource
    private ConvFileCollectService fileCollectService;
    @Resource
    private ConvActiveInterfaceConfigService activeInterfaceConfigService;
    @Resource
    private ConvergeService convergeService;
    @Resource
    private ConvOdsDatasourceConfigService odsDatasourceConfigService;
    @Resource
    private ConvCollectIncrTimeService incrTimeService;
    @Resource
    private FeNodeService feNodeService;
    @Resource
    private ConvTaskService taskService;
    @Resource
    private OdsModelService odsModelService;
    @Resource
    private ModelConfigService modelConfigService;

    @Value("${file-collect.structure-type}")
    private String structureTypeStr;
    @Override
    public void kafkaUpdateFepStatus(String key, String msgBody) {
        if (CharSequenceUtil.isBlank(key)) return;
        log.info("kafkaUpdateFepStatus,key={}", key);
        switch (key) {
            case "tunnel":
                TunnelStatusKafkaDto dto = JSON.parseObject(msgBody, TunnelStatusKafkaDto.class);
                feNodeService.updateTunnel(dto);
                break;
            case "task":
                TaskInfoKafkaDto taskInfoKafkaDto = JSON.parseObject(msgBody, TaskInfoKafkaDto.class);
                ConvTask oldTask = taskService.getOne(new LambdaQueryWrapper<ConvTask>()
                        .eq(ConvTask::getFedTaskId, taskInfoKafkaDto.getTaskId())
                        .eq(ConvTask::getTunnelId, taskInfoKafkaDto.getTunnelId()), false);
                ConvTunnel tunnel = tunnelService.getById(taskInfoKafkaDto.getTunnelId());
                //更新 task
                ConvTask convTask = feNodeService.saveOrUpdateTask(taskInfoKafkaDto, tunnel, oldTask);
                convergeService.sendDsKafka(convTask, oldTask, tunnel.getId());
                break;
            case "taskResultView":
                ResultViewInfoDto resultViewInfoDto = JSON.parseObject(msgBody, ResultViewInfoDto.class);
                ConvTask task = taskService.getOne(new LambdaQueryWrapper<ConvTask>()
                        .eq(ConvTask::getFedTaskId, resultViewInfoDto.getTaskId())
                        .eq(ConvTask::getTunnelId, resultViewInfoDto.getTunnelId()));
                feNodeService.updateTaskResultView(DownloadFileTask.taskDeque, Lists.newArrayList(resultViewInfoDto), task);
                break;
            case "taskResultInterface":
                ResultInterfaceDTO resultInterfaceDTO = JSON.parseObject(msgBody, ResultInterfaceDTO.class);
                ConvTask interfaceTask = taskService.getOne(new LambdaQueryWrapper<ConvTask>()
                        .eq(ConvTask::getFedTaskId, resultInterfaceDTO.getTaskId())
                        .eq(ConvTask::getTunnelId, resultInterfaceDTO.getTunnelId()));
                feNodeService.updateTaskResultInterface(Lists.newArrayList(resultInterfaceDTO), interfaceTask);
                break;
            case "taskResultFile":
                ResultFileInfoDto resultFileInfoDto = JSON.parseObject(msgBody, ResultFileInfoDto.class);
                ConvTask fileTask = taskService.getOne(new LambdaQueryWrapper<ConvTask>()
                        .eq(ConvTask::getFedTaskId, resultFileInfoDto.getTaskId())
                        .eq(ConvTask::getTunnelId, resultFileInfoDto.getTunnelId()));
                feNodeService.updateTaskResultFile(DownloadFileTask.taskDeque, Lists.newArrayList(resultFileInfoDto), fileTask);
                break;
            case "taskLog":
                TaskLogDto taskLogDto = JSON.parseObject(msgBody, TaskLogDto.class);
                ConvTask relateTask = taskService.getOne(new LambdaQueryWrapper<ConvTask>()
                        .eq(ConvTask::getFedTaskId, taskLogDto.getTaskId())
                        .eq(ConvTask::getTunnelId, taskLogDto.getTunnelId()));
                feNodeService.saveOrUpdateLog(Lists.newArrayList(taskLogDto), relateTask.getId());
                break;
            default:
                throw new CommonException("不存在的kafka标识：" + key);
        }
    }

    @Override
    public TunnelMessageDTO getTunnelMessage(ConvTunnel tunnel) {
        TunnelMessageDTO tunnelMessageDTO = new TunnelMessageDTO();
        // 管道基本信息
        // todo: 用普通的convert替换beanutil
        BeanUtil.copyProperties(tunnel, tunnelMessageDTO);
        tunnelMessageDTO.setDependenceTunnelIds(tunnel.getDependenceTunnelId());
        TunnelCMEnum tunnelCMEnum = TunnelCMEnum.of(tunnel.getConvergeMethod());
        switch (Objects.requireNonNull(tunnelCMEnum)) {
            case LIBRARY_TABLE:
            case CDC_LOG:
                tunnelJdbcMessage(tunnel, tunnelMessageDTO);
                break;
            case FILE_MODE:
                tunnelFileMessage(tunnel, tunnelMessageDTO);
                break;
            case ACTIVE_INTERFACE_MODE:
                tunnelInterfaceMessage(tunnel, tunnelMessageDTO);
                break;
            case INTERFACE_MODE:
                break;
            default:
                throw new CommonException("不支持的汇聚方式");
        }
        return tunnelMessageDTO;
    }

    private void tunnelInterfaceMessage(ConvTunnel tunnel, TunnelMessageDTO tunnelMessageDTO) {
        ConvActiveInterfaceConfig config =  activeInterfaceConfigService.getOne(new LambdaQueryWrapper<ConvActiveInterfaceConfig>()
                .eq(ConvActiveInterfaceConfig::getTunnelId, tunnel.getId()));
        tunnelMessageDTO.setActiveInterfaceDTO(ActiveInterfaceDTO.builder().requestBody(config.getRequestBody())
                .authentication(config.getAuthentication())
                .requestMethod(config.getRequestMethod())
                .requestParam(config.getRequestParam())
                .requestUrl(config.getRequestUrl())
                .dataPath(config.getDataPath()).build());
    }

    private void tunnelJdbcMessage(ConvTunnel tunnel, TunnelMessageDTO tunnelMessageDTO) {
        // 库表/日志的读库信息
        JdbcInfoDto jdbcInfoDto = new JdbcInfoDto();
        ConvDsConfig readerDs = odsDatasourceConfigService.getById(tunnel.getReaderDatasourceId());
        jdbcInfoDto.setDsId(readerDs.getId());
        // 库表采集
        if (tunnel.getConvergeMethod().equals(LIBRARY_TABLE.getCode())) {
            // 全量/增量采集
            jdbcInfoDto.setColType(tunnel.getColType());
            // 库到库/库到文件
            jdbcInfoDto.setCollectModel(tunnel.getCollectModel());
            // 单表采集还是自定义sql
            jdbcInfoDto.setColTableType(tunnel.getColTableType());
            // 库到库
            String writeDbType = null;
            if (LibraryTableModelEnum.DATABASE_TO_DATABASE.getCode().equals(jdbcInfoDto.getCollectModel())) {
                ConvDsConfig writerDs = odsDatasourceConfigService.getById(tunnel.getWriterDatasourceId());
                jdbcInfoDto.setDsConfigId(tunnel.getWriterDatasourceId());
                jdbcInfoDto.setHdfsCluster(writerDs.getHdfsCluster());
                writeDbType = writerDs.getDbType();
            }
            // 表以及对应的sql信息
            assembleTableInfoMessage(tunnel, jdbcInfoDto, writeDbType);
        }

        if (tunnel.getConvergeMethod().equals(CDC_LOG.getCode())) {
            String collectRange = tunnel.getCollectRange();
            List<TableInfoDto> tableInfoDtoList = new ArrayList<>();
            String[] split = collectRange.split(",");
            for (String table : split) {
                TableInfoDto tableInfoDto = new TableInfoDto();
                tableInfoDto.setTableName(table);
                tableInfoDtoList.add(tableInfoDto);
            }
            jdbcInfoDto.setTableInfoDtoList(tableInfoDtoList);
        }
        tunnelMessageDTO.setJdbcInfoDto(jdbcInfoDto);
    }

    private void tunnelFileMessage(ConvTunnel tunnel, TunnelMessageDTO tunnelMessageDTO) {
        ConvFileCollect fileCollect = fileCollectService.getOne(new LambdaQueryWrapper<ConvFileCollect>().eq(ConvFileCollect::getTunnelId, tunnel.getId()));
        if(ObjectUtil.isNull(fileCollect)){
            return;
        }
        FileCollectInfoDto fileCollectInfoDto = new FileCollectInfoDto();
        // 文件采集目录
        fileCollectInfoDto.setFileModeCollectDir(fileCollect.getFileModeCollectDir());
        // 文件的采集范围
        fileCollectInfoDto.setFileCollectRange(fileCollect.getFileCollectRange());
        fileCollectInfoDto.setFileStorageMode(fileCollect.getFileStorageMode());
        fileCollectInfoDto.setStructuredDataFlag(fileCollect.getStructuredDataFlag());
        List<String> fileSuffixList = new ArrayList<>();
        Integer fileStorageMode = fileCollect.getFileStorageMode();
        FileStorageTypeEnum type = FileStorageTypeEnum.of(fileStorageMode);
        switch (Objects.requireNonNull(type)) {
            case DATABASE:
                if (CharSequenceUtil.isNotBlank(structureTypeStr)) {
                    fileSuffixList.addAll(Lists.newArrayList(structureTypeStr.split(",")));
                }
                break;
            case DICOM:
                fileSuffixList.add("dcm");
                break;
            case OBJECT_STORAGE:
                break;
        }
        fileCollectInfoDto.setFileSuffix(fileSuffixList);
        fileCollectInfoDto.setIncrFlag(fileCollect.getIncrFlag());
        tunnelMessageDTO.setFileCollectInfoDto(fileCollectInfoDto);
    }


    private void assembleTableInfoMessage(ConvTunnel tunnel, JdbcInfoDto jdbcInfoDto, String writeDbType) {
        // 库表采集范围和sql查询语句
        List<TableInfoDto> tableInfoDtoList = new ArrayList<>();

        // 采集配置
        List<ConvCollectField> collectFieldList = collectFieldService.getTunnelTableConfigs(tunnel.getId());
        for (ConvCollectField collectField : collectFieldList){
            TableInfoDto tableInfoDto = new TableInfoDto();
            if ("HDFS".equals(writeDbType)) {
                // 设置hdfs的一些配置
                setHdfsCollectConfig(collectField, tableInfoDto);
            }else {
                tableInfoDto.setWriterColumns(collectField.getColumnField());
            }
            tableInfoDto.setTableName(collectField.getTableName());
            tableInfoDto.setSqlQuery(collectField.getQuerySql());

            // 增量配置
            if (tunnel.getColType().equals(TunnelColTypeEnum.FREQUENCY_INCREMENT.getValue())) {
                List<IncrColumnDTO> incrConfigList = getIncrConfigList(collectField.getConditionField(),
                        tunnel.getId(), collectField.getTableName());
                tableInfoDto.setIncrConfigList(incrConfigList);
                tableInfoDto.setSeqField(collectField.getConditionField());
            }
            tableInfoDtoList.add(tableInfoDto);
        }
        jdbcInfoDto.setTableInfoDtoList(tableInfoDtoList);
    }

    private List<IncrColumnDTO> getIncrConfigList(String conditionField, Long tunnelId, String tableName){
        List<IncrColumnDTO> incrConfigList = new ArrayList<>();
        String[] incrFields = conditionField.split(",");
        for (String incrField : incrFields){
            IncrColumnDTO incrConfig = getIncrConfig(tunnelId, incrField, tableName);
            incrConfigList.add(incrConfig);
        }
        return incrConfigList;
    }

    private void setHdfsCollectConfig(ConvCollectField collectField, TableInfoDto tableInfoDto){
        // 获取原始模型
        List<StdOriginalModelColumn> modelColumns = odsModelService.getColumnList(collectField.getTableName(), collectField.getSystemCode());
        Long modelId = modelColumns.get(0).getModelId();
        StdOriginalModel stdModel = odsModelService.getModel(modelId);
        // 设置hdfs写入路径
        tableInfoDto.setHdfsPath(stdModel.getStoragePath());
        // 设置datax写入字段格式
        tableInfoDto.setWriterColumns(doGetHiveColumns(modelColumns, collectField.getColumnField()));

        // 查询其他hive配置
        ModelConfig modelConfig = modelConfigService.getModelConfig(modelId);
        if(ObjectUtil.isNotNull(modelConfig)){
            tableInfoDto.setHiveFileType(modelConfig.getTableType());
            DbConfigColumnDTO dto = JSON.parseObject(modelConfig.getDbConfig(), DbConfigColumnDTO.class);
            tableInfoDto.setHivePartitionColumn(CollUtil.isEmpty(dto.getPartitionKey()) ? null : dto.getPartitionKey().get(0));
        }
    }

    /**
     * 创建hive库columns映射关系
     */
    private String doGetHiveColumns(List<StdOriginalModelColumn> modelColumnList, String columnField) {
        if (CollUtil.isEmpty(modelColumnList)) {
            return null;
        }
        if (CharSequenceUtil.isNotEmpty(columnField)) {
            List<String> existNames = Arrays.asList(columnField.split(","));
            modelColumnList = modelColumnList.stream()
                    .filter(column -> existNames.contains(column.getNameEn()))
                    .sorted(Comparator.comparing(StdOriginalModelColumn::getSeqNo))
                    .collect(Collectors.toList());
        }
        StringBuilder sb = new StringBuilder();
        //dataType=1为采集标准数据 write全部转换为varchar
        for (StdOriginalModelColumn modelColumn : modelColumnList) {
            String dataType = transformDataType(modelColumn.getFieldType());
            sb.append("{\"name\":\"").append(modelColumn.getNameEn())
                    .append("\",\"type\":\"").append(dataType)
                    .append("\"}").append(",\n");
        }
        String substring = sb.substring(0, sb.length() - 2);
        return substring
                .concat(",\n")
                .concat("{\"name\":\"" + "xds_id")
                .concat("\",\"type\":\"" + "BIGINT" + "\"}")
                .concat(",\n")
                .concat("{\"name\":\"" + "load_time")
                .concat("\",\"type\":\"" + "TIMESTAMP"  + "\"}");
    }

    private String transformDataType(String fieldType) {
        fieldType = fieldType.toLowerCase();
        String transformStr;
        switch (fieldType) {
            case "varchar":
            case "varchar2":
            case "json":
                transformStr = "VARCHAR";
                break;
            case "text":
            case "blob":
            case "clob":
                transformStr = "STRING";
                break;
            case "int2":
            case "int4":
                transformStr = "INT";
                break;
            case "int8":
                transformStr = "BIGINT";
                break;
            case "date":
                transformStr = "DATE";
                break;
            case "datetime":
            case "timestamp":
            case "timestampz":
                transformStr = "TIMESTAMP";
                break;
            case "numeric":
            case "number":
                transformStr = "DOUBLE";
                break;
            case "float8":
                transformStr = "FLOAT";
                break;
            default:
                if (fieldType.startsWith("timestamp")){
                    transformStr = "TIMESTAMP";
                }
                else {
                    transformStr = "STRING";
                }
                break;
        }
        return transformStr.toUpperCase();
    }


    private IncrColumnDTO getIncrConfig(Long tunnelId, String column, String tableName) {
        ConvCollectIncrTime incrTime = incrTimeService.getOne(
                new LambdaQueryWrapper<ConvCollectIncrTime>()
                .eq(ConvCollectIncrTime::getTunnelId, tunnelId)
                .eq(ConvCollectIncrTime::getIncrField, column)
                .eq(ConvCollectIncrTime::getTableName, tableName));
        if (ObjectUtil.isEmpty(incrTime)){
            log.error("管道{}-表{}-字段{}没有增量配置",tunnelId, tableName, column);
            throw new RuntimeException();
        }
        IncrColumnDTO incrColumnDTO = IncrColumnDTO.builder()
                .columnName(incrTime.getIncrField())
                .tableName(incrTime.getTableName())
                .incrType(Integer.valueOf(incrTime.getIncrFieldType()))
                .build();
        // 数字
        if (incrColumnDTO.getIncrType() == 1) {
            incrColumnDTO.setSeqStartPoint(CharSequenceUtil.isBlank(incrTime.getLatestSeq()) ?
                    incrTime.getSeqStartPoint() : incrTime.getLatestSeq());
        } else {
           incrColumnDTO.setTimeStartPoint(CharSequenceUtil.isBlank(incrTime.getLatestTime()) ?
                   incrTime.getTimeStartPoint() : incrTime.getLatestTime());
        }
        incrColumnDTO.setIncrMaxSql(incrTime.getMaxSql());
        return incrColumnDTO;
    }
}
