package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.dao.entity.ConvOriginalColumn;
import com.lrhealth.data.converge.dao.entity.ConvOriginalTable;
import com.lrhealth.data.converge.dao.service.ConvOriginalColumnService;
import com.lrhealth.data.converge.dao.service.ConvOriginalTableService;
import com.lrhealth.data.converge.model.dto.ColumnInfoDTO;
import com.lrhealth.data.converge.model.dto.OriginalStructureDto;
import com.lrhealth.data.converge.model.dto.OriginalTableCountDto;
import com.lrhealth.data.converge.model.dto.OriginalTableDto;
import com.lrhealth.data.converge.service.ImportOriginalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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

    @Transactional(rollbackFor = Exception.class)
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
        processOriginalTable(originalTableDtoList, orgCode, sysCode, dsConfigId, dateTime);
        processOriginalColumn(originalTableDtoList, orgCode, sysCode, dsConfigId, dateTime);

    }

    @Override
    public void updateOriginalTableCount(OriginalTableCountDto tableCountDto) {
        List<ConvOriginalTable> originalTableList = originalTableService.list(new LambdaQueryWrapper<ConvOriginalTable>()
                .eq(ConvOriginalTable::getSysCode, tableCountDto.getSysCode())
                .eq(ConvOriginalTable::getConvDsConfId, tableCountDto.getDsConfId()));
        Map<String, Long> tableCountMap = tableCountDto.getTableCountMap();
        List<ConvOriginalTable> tableList = CollUtil.newArrayList();
        for (ConvOriginalTable originalTable : originalTableList) {
            if (!tableCountMap.containsKey(originalTable.getNameEn())) {
                continue;
            }
            ConvOriginalTable table = ConvOriginalTable.builder()
                    .id(originalTable.getId())
                    .dataCount(tableCountMap.get(originalTable.getNameEn()))
                    .updateTime(LocalDateTime.now()).build();
            tableList.add(table);
        }
        originalTableService.updateBatchById(tableList);
    }

    private void processOriginalTable(List<OriginalTableDto> tableList, String orgCode, String sysCode, Integer dsConfigId, LocalDateTime saveTime) {
        List<ConvOriginalTable> convOriginalTableList = CollUtil.newArrayList();
        List<ConvOriginalTable> storedTables = originalTableService.list(new LambdaQueryWrapper<ConvOriginalTable>()
                .eq(ConvOriginalTable::getSysCode, sysCode)
                .eq(ConvOriginalTable::getConvDsConfId, dsConfigId)
                .orderByDesc(ConvOriginalTable::getCreateTime));
        for (OriginalTableDto tableDto : tableList){
            List<ConvOriginalTable> tables = storedTables.stream().filter(storeTable -> storeTable.getNameEn().equals(tableDto.getTableName())).collect(Collectors.toList());
            ConvOriginalTable originalTable;
            if (CollUtil.isNotEmpty(tables)){
                // 已存在的进行更新
                originalTable = ConvOriginalTable.builder()
                        .id(tables.get(0).getId())
                        .createTime(saveTime)
                        .updateTime(saveTime)
                        .build();

            }else {
                originalTable = ConvOriginalTable.builder()
                        .nameEn(tableDto.getTableName())
                        .nameCn(tableDto.getTableRemarks())
                        .convDsConfId(dsConfigId)
                        .orgCode(orgCode)
                        .sysCode(sysCode)
                        .createTime(saveTime)
                        .dataSource(0)
                        .modelName(tableDto.getTableName())
                        .modelDescription(tableDto.getTableRemarks())
                        .build();
            }
            convOriginalTableList.add(originalTable);
            storedTables.removeAll(tables);
        }

        // 删除之前的库里存在的多余的表
//        if (CollUtil.isNotEmpty(storedTables)){
//            originalTableService.removeBatchByIds(storedTables);
//        }

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
                            .build();
                }
                i++;
                originalColumnList.add(convOriginalColumn);
                convOriginalColumns.removeAll(storedSameNameList);
            }
            deleteColumnList.addAll(convOriginalColumns);
        }
//        if (CollUtil.isNotEmpty(deleteColumnList)){
//            originalColumnService.removeBatchByIds(deleteColumnList);
//        }
        originalColumnService.saveOrUpdateBatch(originalColumnList);
    }

    /**
     * 获取字段类型
     */
    private String getFieldType(Map<String, String> fieldTypeMap, String fieldType) {
        if (StrUtil.isEmpty(fieldType)) {
            return StrUtil.EMPTY;
        }
        fieldType = fieldType.toLowerCase();
        for (Map.Entry<String, String> entry : fieldTypeMap.entrySet()) {
            if (fieldType.contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        return StrUtil.EMPTY;
    }
}
