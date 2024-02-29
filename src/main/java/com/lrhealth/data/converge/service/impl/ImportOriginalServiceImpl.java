package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.dao.entity.ConvOriginalColumn;
import com.lrhealth.data.converge.dao.entity.ConvOriginalTable;
import com.lrhealth.data.converge.dao.service.ConvFieldTypeService;
import com.lrhealth.data.converge.dao.service.ConvOdsDatasourceConfigService;
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

    @Resource
    private ConvOdsDatasourceConfigService convOdsDatasourceConfigService;

    @Resource
    private ConvFieldTypeService convFieldTypeService;


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
        for (OriginalTableDto tableDto : tableList){
            ConvOriginalTable table = originalTableService.getOne(new LambdaQueryWrapper<ConvOriginalTable>()
                    .eq(ConvOriginalTable::getNameEn, tableDto.getTableName())
                    .eq(ConvOriginalTable::getSysCode, sysCode)
                    .eq(ConvOriginalTable::getConvDsConfId, dsConfigId));
            if (ObjectUtil.isNotNull(table)){
                // 已存在的进行更新
                ConvOriginalTable originalTable = ConvOriginalTable.builder()
                        .id(table.getId())
                        .createTime(saveTime)
                        .updateTime(saveTime)
                        .build();
                convOriginalTableList.add(originalTable);
                continue;
            }
            ConvOriginalTable originalTable = ConvOriginalTable.builder()
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
            convOriginalTableList.add(originalTable);
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
        for (OriginalTableDto tableDto : tableList) {
            Long tableId = tableNameIdMapping.get(tableDto.getTableName());
            if (tableId == null) {
                log.error("original table [{}] 表不存在", tableDto.getTableName());
                continue;
            }

            List<ConvOriginalColumn> convOriginalColumns = originalColumnService.list(new LambdaQueryWrapper<ConvOriginalColumn>()
                    .eq(ConvOriginalColumn::getTableId, tableId));
            List<String> nameList = convOriginalColumns.stream().map(ConvOriginalColumn::getColumnName).collect(Collectors.toList());

            List<ColumnInfoDTO> columnInfoDTOS = tableDto.getColumnInfoDTOS();
            int i = 1;
            for (ColumnInfoDTO columnInfoDTO : columnInfoDTOS) {
                if (nameList.contains(columnInfoDTO.getColumnName())){
                    continue;
                }
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
                        .columnName(columnInfoDTO.getColumnName())
                        .columnDescription(columnInfoDTO.getRemark())
                        .columnFieldLength(columnInfoDTO.getColumnLength())
                        .build();
                i++;
                originalColumnList.add(convOriginalColumn);
            }
        }
        originalColumnService.saveBatch(originalColumnList);
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
