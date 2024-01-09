package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
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

    @Override
    public void importConvOriginal(OriginalStructureDto structureDto) {
        String orgCode = structureDto.getOrgCode();
        String sysCode = structureDto.getSysCode();
        List<OriginalTableDto> originalTableDtoList = structureDto.getOriginalTables();
        if (CollUtil.isEmpty(originalTableDtoList)){
            return;
        }
        Integer dsConfigId = structureDto.getDsConfId();

        processOriginalTable(originalTableDtoList, orgCode, sysCode, dsConfigId);
        processOriginalColumn(originalTableDtoList, orgCode, sysCode, dsConfigId);

    }

    @Override
    public void updateOriginalTableCount(OriginalTableCountDto tableCountDto) {
        List<ConvOriginalTable> originalTableList = originalTableService.list(new LambdaQueryWrapper<ConvOriginalTable>()
                .eq(ConvOriginalTable::getSysCode, tableCountDto.getSysCode())
                .eq(ConvOriginalTable::getConvDsConfId, tableCountDto.getDsConfId()));
        Map<String, Long> tableCountMap = tableCountDto.getTableCountMap();
        List<ConvOriginalTable> tableList = CollUtil.newArrayList();
        for (ConvOriginalTable originalTable : originalTableList){
            if (!tableCountMap.containsKey(originalTable.getNameEn())){
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

    private void processOriginalTable(List<OriginalTableDto> tableList, String orgCode, String sysCode, Integer dsConfigId){
        originalTableService.remove(new LambdaQueryWrapper<ConvOriginalTable>().eq(ConvOriginalTable::getOrgCode, orgCode)
                .eq(ConvOriginalTable::getSysCode, sysCode)
                .eq(ConvOriginalTable::getConvDsConfId, dsConfigId));
        List<ConvOriginalTable> convOriginalTableList = CollUtil.newArrayList();
        tableList.forEach(tableDto -> {
            ConvOriginalTable originalTable = ConvOriginalTable.builder().nameEn(tableDto.getTableName()).nameCn(tableDto.getTableRemarks())
                    .convDsConfId(dsConfigId).orgCode(orgCode).sysCode(sysCode)
                    .createTime(LocalDateTime.now()).build();
            convOriginalTableList.add(originalTable);
        });
        originalTableService.saveBatch(convOriginalTableList);
    }

    private void processOriginalColumn(List<OriginalTableDto> tableList, String orgCode, String sysCode, Integer dsConfigId){
        List<ConvOriginalTable> originalTableList = originalTableService.list(new LambdaQueryWrapper<ConvOriginalTable>().eq(ConvOriginalTable::getOrgCode, orgCode)
                .eq(ConvOriginalTable::getSysCode, sysCode).eq(ConvOriginalTable::getConvDsConfId, dsConfigId));
        Map<String, Long> tableNameIdMapping = originalTableList.stream().collect(Collectors.toMap(ConvOriginalTable::getNameEn, ConvOriginalTable::getId));
        originalColumnService.remove(new LambdaQueryWrapper<ConvOriginalColumn>()
                .eq(ConvOriginalColumn::getOrgCode, orgCode).eq(ConvOriginalColumn::getSysCode, sysCode)
                .in(ConvOriginalColumn::getTableId, tableNameIdMapping.values()));

        List<ConvOriginalColumn> originalColumnList = CollUtil.newArrayList();
        for (OriginalTableDto tableDto : tableList){
            Long tableId = tableNameIdMapping.get(tableDto.getTableName());
            if (tableId == null){
                log.error("original table [{}] 表不存在", tableDto.getTableName());
                continue;
            }
            List<ColumnInfoDTO> columnInfoDTOS = tableDto.getColumnInfoDTOS();
            int i =1;
            for (ColumnInfoDTO columnInfoDTO : columnInfoDTOS){
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
                        .createTime(LocalDateTime.now())
                        .build();
                i++;
                originalColumnList.add(convOriginalColumn);
            }
        }
        originalColumnService.saveBatch(originalColumnList);

    }

}
