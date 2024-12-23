package com.lrhealth.data.converge.dao.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.common.util.DatabaseTypeUtil;
import com.lrhealth.data.converge.dao.entity.ConvFieldType;
import com.lrhealth.data.converge.dao.mapper.ConvFieldTypeMapper;
import com.lrhealth.data.converge.dao.service.ConvFieldTypeService;
import com.lrhealth.data.converge.dao.service.ConvOdsDatasourceConfigService;
import com.lrhealth.data.converge.model.dto.DbTypeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 数据源字段类型映射
 * </p>
 *
 * @author flzhang
 * @since 2024-01-23
 */
@Slf4j
@Service
public class ConvFieldTypeServiceImpl extends ServiceImpl<ConvFieldTypeMapper, ConvFieldType> implements ConvFieldTypeService {

    @Resource
    private ConvOdsDatasourceConfigService odsDatasourceConfigService;

    @Override
    public void saveFieldType(List<DbTypeDto> fieldList, Integer convDsConfigId) {
        if (CollUtil.isEmpty(fieldList) || convDsConfigId == null){
            log.error("保存库表数据类型失败，map={}或dsConfigId={}为空值", fieldList, convDsConfigId);
            return;
        }
        String dbType = odsDatasourceConfigService.getDbType(convDsConfigId);
        if (CharSequenceUtil.isBlank(dbType)){
            log.error("无法通过dsConfigId={}获取到数据库类型", convDsConfigId);
            return;
        }
        saveFieldMap(fieldList, dbType);
    }

    private void saveFieldMap(List<DbTypeDto> fieldList, String dbType){
        List<ConvFieldType> fieldTypeList = this.list(new LambdaQueryWrapper<ConvFieldType>()
                .eq(ConvFieldType::getClientSource, dbType)
                .eq(ConvFieldType::getPlatformSource, "java"));
        List<DbTypeDto> newFieldList = fieldList.stream()
                .filter(input -> fieldTypeList.stream()
                        .noneMatch(exist -> input.getTypeName().equals(exist.getClientFieldType())
                        && input.getDataType().equals(exist.getClientDataType()))).collect(Collectors.toList());
        List<ConvFieldType> addList = new ArrayList<>();
        for (DbTypeDto fieldType : newFieldList){
            String dataType = fieldType.getDataType();
            String fieldName = fieldType.getTypeName();
            ConvFieldType type = ConvFieldType.builder()
                    .clientSource(dbType)
                    .clientDataType(dataType)
                    .clientFieldType(fieldName)
                    .platformSource("java")
                    .platformDataType(dataType)
                    .platformFieldType(DatabaseTypeUtil.getJavaType(dataType))
                    .build();
            addList.add(type);
        }
        this.saveBatch(addList);
    }


    @Override
    public void saveFieldType(List<DbTypeDto> fieldList, String dbType) {
        if (CollUtil.isEmpty(fieldList) || CharSequenceUtil.isBlank(dbType)){
            log.error("保存库表数据类型失败，map={}或dbType={}为空值", fieldList, dbType);
            return;
        }
        saveFieldMap(fieldList, dbType);
    }
}
