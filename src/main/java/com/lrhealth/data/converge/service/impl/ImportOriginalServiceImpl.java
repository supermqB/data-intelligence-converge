package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.dao.entity.ConvOriginalColumn;
import com.lrhealth.data.converge.dao.entity.ConvOriginalColumnMap;
import com.lrhealth.data.converge.dao.entity.ConvOriginalTable;
import com.lrhealth.data.converge.dao.entity.ConvOriginalTableMap;
import com.lrhealth.data.converge.dao.service.ConvOriginalColumnMapService;
import com.lrhealth.data.converge.dao.service.ConvOriginalColumnService;
import com.lrhealth.data.converge.dao.service.ConvOriginalTableMapService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ConvOriginalTableMapService convOriginalTableMapService;

    @Resource
    private ConvOriginalColumnMapService convOriginalColumnMapService;

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
        tableList.forEach(tableDto -> {
            ConvOriginalTable originalTable = ConvOriginalTable.builder().nameEn(tableDto.getTableName()).nameCn(tableDto.getTableRemarks())
                    .convDsConfId(dsConfigId).orgCode(orgCode).sysCode(sysCode)
                    .createTime(saveTime).build();
            convOriginalTableList.add(originalTable);
        });
        originalTableService.saveBatch(convOriginalTableList);
    }

    private void processOriginalColumn(List<OriginalTableDto> tableList, String orgCode, String sysCode, Integer dsConfigId, LocalDateTime saveTime) {
        List<ConvOriginalTable> originalTableList = originalTableService.list(new LambdaQueryWrapper<ConvOriginalTable>().eq(ConvOriginalTable::getOrgCode, orgCode)
                .eq(ConvOriginalTable::getSysCode, sysCode)
                .eq(ConvOriginalTable::getConvDsConfId, dsConfigId)
                .eq(ConvOriginalTable::getCreateTime, saveTime));
        if (CollUtil.isEmpty(originalTableList)) {
            return;
        }

        List<ConvOriginalTableMap> convOriginalTableMapList = CollUtil.newArrayList();
        Map<String, Long> tableNameIdMapping = new HashMap<>(16);
        originalTableList.forEach(convOriginalTable -> {
            ConvOriginalTableMap originalTableMap = ConvOriginalTableMap.builder().orgCode(orgCode).sysCode(sysCode).originalModelId(convOriginalTable.getId()).originalModelName(convOriginalTable.getNameCn()).originalModelDescription(convOriginalTable.getDescription()).build();
            convOriginalTableMapList.add(originalTableMap);
            tableNameIdMapping.put(convOriginalTable.getNameEn(), convOriginalTable.getId());
        });

        convOriginalTableMapService.saveBatch(convOriginalTableMapList);

        List<ConvOriginalColumn> originalColumnList = CollUtil.newArrayList();
        for (OriginalTableDto tableDto : tableList) {
            Long tableId = tableNameIdMapping.get(tableDto.getTableName());
            if (tableId == null) {
                log.error("original table [{}] 表不存在", tableDto.getTableName());
                continue;
            }
            List<ColumnInfoDTO> columnInfoDTOS = tableDto.getColumnInfoDTOS();
            int i = 1;
            for (ColumnInfoDTO columnInfoDTO : columnInfoDTOS) {
                ConvOriginalColumn convOriginalColumn = ConvOriginalColumn.builder()
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
                        .build();
                i++;
                originalColumnList.add(convOriginalColumn);
            }
        }

        originalColumnService.saveBatch(originalColumnList);

        List<ConvOriginalColumn> convOriginalColumnList = originalColumnService.list(new LambdaQueryWrapper<ConvOriginalColumn>().eq(ConvOriginalColumn::getOrgCode, orgCode)
                .eq(ConvOriginalColumn::getSysCode, sysCode)
                .eq(ConvOriginalColumn::getCreateTime, saveTime));
        if (CollUtil.isEmpty(convOriginalColumnList)) {
            return;
        }

        List<ConvOriginalColumnMap> originalColumnMapList = CollUtil.newArrayList();
        convOriginalColumnList.forEach(convOriginalColumn -> {
            ConvOriginalColumnMap convOriginalColumnMap = ConvOriginalColumnMap.builder().orgCode(orgCode).sysCode(sysCode).oriModelColumnId(convOriginalColumn.getId()).columnName(convOriginalColumn.getNameEn()).columnDescription(convOriginalColumn.getNameCn()).columnFieldType(convOriginalColumn.getFieldType()).columnFieldLength(convOriginalColumn.getFieldTypeLength()).oriColumnFlag("1").build();
            originalColumnMapList.add(convOriginalColumnMap);
        });

        convOriginalColumnMapService.saveBatch(originalColumnMapList);
    }
}
