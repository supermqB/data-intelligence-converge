package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.db.DbConnection;
import com.lrhealth.data.converge.common.db.DbConnectionManager;
import com.lrhealth.data.converge.common.enums.ProbeModelEnum;
import com.lrhealth.data.converge.dao.entity.*;
import com.lrhealth.data.converge.dao.service.*;
import com.lrhealth.data.converge.model.dto.*;
import com.lrhealth.data.converge.service.ImportOriginalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jinmengyu
 * @date 2024-01-04
 */
@Slf4j
@Service
public class ImportOriginalServiceImpl implements ImportOriginalService {
    @Resource
    private ConvOriginalTableService originalTableService;
    @Resource
    private ConvOriginalColumnService originalColumnService;
    @Resource
    private StdOriginalModelService stdModelService;
    @Resource
    private ConvFieldTypeService fieldTypeService;
    @Resource
    private DbConnectionManager dbConnectionManager;
    @Resource
    private ConvOriginalDataService originalDataService;
    @Resource
    private ConvOriginalProbeService originalProbeService;


    @Override
    public void importPlatformDataType(DataSourceInfoDto dto) {
        List<DbTypeDto> fieldList = new ArrayList<>();
        DbConnection dbConnection = DbConnection.builder()
                .dbUrl(dto.getJdbcUrl())
                .dbUserName(dto.getUsername())
                .dbPassword(dto.getPassword())
                .dbDriver(dto.getDriverName())
                .build();
        try (Connection connection = dbConnectionManager.getConnection(dbConnection)){
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet typeInfo = metaData.getTypeInfo();
            while (typeInfo.next()){
                String typeName = typeInfo.getString("TYPE_NAME");
                String dataType = typeInfo.getString("DATA_TYPE");
                String precision = typeInfo.getString("PRECISION");
                log.info("获取到数据类型={}，编号={}，精度={}", typeName, dataType, precision);
                if (fieldList.stream().noneMatch(field -> field.getTypeName().equals(typeName) && field.getDataType().equals(dataType))) {
                    fieldList.add(DbTypeDto.builder()
                            .dataType(dataType)
                            .typeName(typeName)
                            .precision(precision).build());
                }
            }
        }catch (SQLException e){
            log.error("get data type error, {}", ExceptionUtils.getStackTrace(e));
        }
        fieldTypeService.saveFieldType(fieldList, dto.getDbType());
    }

    @Override
    public void importConvOriginal(OriginalStructureDto structureDto) {
        String orgCode = structureDto.getOrgCode();
        String sysCode = structureDto.getSysCode();
        List<OriginalTableDto> originalTableDtoList = structureDto.getOriginalTables();
        if (CollUtil.isEmpty(originalTableDtoList)) {
            return;
        }
        Integer dsConfigId = structureDto.getDsConfId();
        LocalDateTime dateTime = LocalDateTime.now();
        // 原始结构表
        processOriginalTable(structureDto, dateTime);
        // 原始结构字段
        processOriginalColumn(originalTableDtoList, orgCode, sysCode, dsConfigId, dateTime);

        // 处理原始表和模型的关联, 回填模型
        backFillModel(orgCode, sysCode, dsConfigId, dateTime);
    }

    @Override
    public void originalTableProbe(OriDataProbeDTO probeDTO) {
        List<ConvOriginalTable> originalTableList = originalTableService.list(
                new LambdaQueryWrapper<ConvOriginalTable>()
                .eq(ConvOriginalTable::getSysCode, probeDTO.getSysCode())
                .eq(ConvOriginalTable::getConvDsConfId, probeDTO.getDsConfId()));
        for (ConvOriginalTable originalTable : originalTableList) {
            if (!probeDTO.getTableName().equalsIgnoreCase(originalTable.getNameEn())){
                continue;
            }
            ConvOriginalTable table = ConvOriginalTable.builder()
                    .id(originalTable.getId())
                    .dataCount(probeDTO.getDataCount())
                    .dataSize(probeDTO.getDataSize())
                    .updateTime(LocalDateTime.now())
                    .build();
            originalTableService.updateById(table);

            // 样例数据
            String dataList = probeDTO.getOriDataList();
            ConvOriginalData data = ConvOriginalData.builder()
                    .tableId(originalTable.getId())
                    .tableName(originalTable.getNameEn())
                    .dsConfigId(Long.valueOf(originalTable.getConvDsConfId()))
                    .data(dataList)
                    .build();
            originalDataService.save(data);

            // 空值率
            List<ColumnNullableDTO> nullableList = probeDTO.getNullableList();
            List<ConvOriginalProbe> probeList = new ArrayList<>();
            for (ColumnNullableDTO nullableDTO : nullableList){
                ConvOriginalProbe probe = ConvOriginalProbe.builder()
                        .probeModel(ProbeModelEnum.COLUMN_NULLABLE.getCode())
                        .tableId(originalTable.getId())
                        .tableName(originalTable.getNameEn())
                        .columnName(nullableDTO.getColumnName())
                        .nullable(nullableDTO.getNullable())
                        .build();
                probeList.add(probe);
            }
            originalProbeService.saveBatch(probeList);
        }
    }

    public void processOriginalTable(OriginalStructureDto structureDto, LocalDateTime saveTime) {
        List<ConvOriginalTable> convOriginalTableList = CollUtil.newArrayList();
        List<ConvOriginalTable> storedTables = originalTableService.list(new LambdaQueryWrapper<ConvOriginalTable>()
                .eq(ConvOriginalTable::getSysCode, structureDto.getSysCode())
                .eq(ConvOriginalTable::getConvDsConfId, structureDto.getDsConfId())
                .orderByDesc(ConvOriginalTable::getCreateTime));
        for (OriginalTableDto tableDto : structureDto.getOriginalTables()){
            List<ConvOriginalTable> tables = storedTables.stream()
                    .filter(storeTable -> storeTable.getNameEn().equals(tableDto.getTableName()))
                    .collect(Collectors.toList());
            ConvOriginalTable originalTable;
            if (CollUtil.isNotEmpty(tables)){
                // 已存在的进行更新
                originalTable = ConvOriginalTable.builder()
                        .id(tables.get(0).getId())
                        .probeTime(tableDto.getProbeTime())
                        .nameCn(tableDto.getTableRemarks())
                        .createTime(saveTime)
                        .updateTime(saveTime)
                        .build();
            }else {
                originalTable = ConvOriginalTable.builder()
                        .nameEn(tableDto.getTableName())
                        .nameCn(tableDto.getTableRemarks())
                        .convDsConfId(structureDto.getDsConfId())
                        .orgCode(structureDto.getOrgCode())
                        .sysCode(structureDto.getSysCode())
                        .createTime(saveTime)
                        .dataSource(0)
                        .probeTime(tableDto.getProbeTime())
                        .build();
                // 已存在的模型，重新绑定
                boundModel(originalTable);
            }
            convOriginalTableList.add(originalTable);
            storedTables.removeAll(tables);
        }
        originalTableService.saveOrUpdateBatch(convOriginalTableList);
    }

    private void processOriginalColumn(List<OriginalTableDto> tableList, String orgCode, String sysCode, Integer dsConfigId, LocalDateTime saveTime) {
        List<ConvOriginalTable> originalTableList = originalTableService.list(new LambdaQueryWrapper<ConvOriginalTable>().eq(ConvOriginalTable::getOrgCode, orgCode)
                .eq(CharSequenceUtil.isNotBlank(sysCode), ConvOriginalTable::getSysCode, sysCode)
                .eq(ConvOriginalTable::getConvDsConfId, dsConfigId)
                .eq(ConvOriginalTable::getCreateTime, saveTime));
        Map<String, Long> tableNameIdMapping = originalTableList.stream().collect(Collectors.toMap(ConvOriginalTable::getNameEn, ConvOriginalTable::getId));

        List<ConvOriginalColumn> originalColumnList = CollUtil.newArrayList();

        List<ConvOriginalColumn> deleteColumnList = CollUtil.newArrayList();

        List<DbTypeDto> fieldList = new ArrayList<>();

        for (OriginalTableDto tableDto : tableList) {
            Long tableId = tableNameIdMapping.get(tableDto.getTableName());
            if (tableId == null) {
                log.error("original table [{}] 表不存在", tableDto.getTableName());
                continue;
            }

            List<ConvOriginalColumn> convOriginalColumns = originalColumnService.list(
                    new LambdaQueryWrapper<ConvOriginalColumn>()
                    .eq(ConvOriginalColumn::getTableId, tableId));

            List<ColumnInfoDTO> columnInfoDTOS = tableDto.getColumnInfoDTOS();
            int i = 1;
            for (ColumnInfoDTO columnInfoDTO : columnInfoDTOS) {
                List<ConvOriginalColumn> storedSameNameList = convOriginalColumns.stream()
                        .filter(column -> column.getColumnName().equals(columnInfoDTO.getColumnName()))
                        .collect(Collectors.toList());
                ConvOriginalColumn convOriginalColumn;
                if (CollUtil.isNotEmpty(storedSameNameList)){
                    convOriginalColumn = ConvOriginalColumn.builder()
                            .id(storedSameNameList.get(0).getId())
                            .nameCn(columnInfoDTO.getRemark())
                            .seqNo(i)
                            .primaryKeyFlag(String.valueOf(columnInfoDTO.getPrimaryKeyFlag()))
                            .requiredFlag(String.valueOf(columnInfoDTO.getNullable()))
                            .fieldType(columnInfoDTO.getColumnTypeName())
                            .fieldTypeLength(columnInfoDTO.getColumnLength())
                            .createTime(saveTime)
                            .columnDescription(columnInfoDTO.getRemark())
                            .columnFieldLength(columnInfoDTO.getColumnLength())
                            .dataType(columnInfoDTO.getDataType())
                            .build();
                }else {
                     convOriginalColumn = ConvOriginalColumn.builder()
                            .tableId(tableId)
                            .nameCn(columnInfoDTO.getRemark())
                            .nameEn(columnInfoDTO.getColumnName())
                            .seqNo(i)
                            .primaryKeyFlag(String.valueOf(columnInfoDTO.getPrimaryKeyFlag()))
                            .requiredFlag(String.valueOf(columnInfoDTO.getNullable()))
                            .orgCode(orgCode).sysCode(sysCode)
                            .fieldType(columnInfoDTO.getColumnTypeName())
                            .fieldTypeLength(columnInfoDTO.getColumnLength())
                            .createTime(saveTime)
                            .columnName(columnInfoDTO.getColumnName())
                            .columnDescription(columnInfoDTO.getRemark())
                            .columnFieldLength(columnInfoDTO.getColumnLength())
                             .dataType(columnInfoDTO.getDataType())
                            .build();
                }
                i++;
                originalColumnList.add(convOriginalColumn);
                convOriginalColumns.removeAll(storedSameNameList);

                if (CharSequenceUtil.isNotBlank(columnInfoDTO.getDataType()) && fieldList.stream()
                        .noneMatch(field -> field.getDataType().equals(columnInfoDTO.getDataType())
                        && field.getTypeName().equals(columnInfoDTO.getColumnTypeName()))){
                    fieldList.add(DbTypeDto.builder()
                            .dataType(columnInfoDTO.getDataType())
                            .typeName(columnInfoDTO.getColumnTypeName())
                            .build());
                }
            }
            deleteColumnList.addAll(convOriginalColumns);
        }
        originalColumnService.saveOrUpdateBatch(originalColumnList);


        // 对于此次库表结构的数据类型进行处理
        fieldTypeService.saveFieldType(fieldList, dsConfigId);
    }

    private void boundModel(ConvOriginalTable originalTable){
        List<StdOriginalModel> modelList = stdModelService.list(new LambdaQueryWrapper<StdOriginalModel>()
                .eq(StdOriginalModel::getNameEn, originalTable.getNameEn())
                .eq(StdOriginalModel::getSysCode, originalTable.getSysCode())
                .orderByDesc(StdOriginalModel::getCreateTime));
        if (CollUtil.isEmpty(modelList)){
            return;
        }
        StdOriginalModel existModel = modelList.get(0);
        originalTable.setModelId(existModel.getId());
        originalTable.setModelName(existModel.getNameEn());
        originalTable.setModelDescription(existModel.getDescription());
    }

    private void backFillModel(String orgCode, String sysCode, Integer dsConfigId, LocalDateTime saveTime){
        List<ConvOriginalTable> originalTableList = originalTableService.list(new LambdaQueryWrapper<ConvOriginalTable>().eq(ConvOriginalTable::getOrgCode, orgCode)
                .eq(CharSequenceUtil.isNotBlank(sysCode), ConvOriginalTable::getSysCode, sysCode)
                .eq(ConvOriginalTable::getConvDsConfId, dsConfigId)
                .eq(ConvOriginalTable::getCreateTime, saveTime));
        List<ConvOriginalTable> convOriginalTables = originalTableList.stream().filter(orgTable -> orgTable.getModelId() != null).collect(Collectors.toList());
        for(ConvOriginalTable convOriginalTable : convOriginalTables){
            Long modelId =convOriginalTable.getModelId();
            stdModelService.updateById(StdOriginalModel.builder()
                    .id(modelId)
                    .originalId(convOriginalTable.getId())
                    .updateTime(LocalDateTime.now()).build());
        }
    }
}
