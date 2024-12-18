package com.lrhealth.data.converge.dao.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.common.util.DatabaseTypeUtil;
import com.lrhealth.data.converge.dao.entity.ConvFieldType;
import com.lrhealth.data.converge.dao.entity.ConvOdsDatasourceConfig;
import com.lrhealth.data.converge.dao.mapper.ConvFieldTypeMapper;
import com.lrhealth.data.converge.dao.service.ConvFieldTypeService;
import com.lrhealth.data.converge.dao.service.ConvOdsDatasourceConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    public void saveFieldType(Map<String, String> fieldMap, Integer convDsConfigId) {
        if (CollUtil.isEmpty(fieldMap) || convDsConfigId == null){
            log.error("保存库表数据类型失败，map={}或dsConfigId={}为空值", fieldMap, convDsConfigId);
            return;
        }
        ConvOdsDatasourceConfig datasourceConfig = odsDatasourceConfigService.getById(convDsConfigId);
        if (ObjectUtil.isEmpty(datasourceConfig)){
            log.error("不存在的数据源id[{}]", convDsConfigId);
            return;
        }
        List<ConvFieldType> fieldTypeList = this.list(new LambdaQueryWrapper<ConvFieldType>()
                .eq(ConvFieldType::getClientSource, datasourceConfig.getDbType())
                .eq(ConvFieldType::getPlatformSource, "java"));
        Map<String, String> existMap = fieldTypeList.stream().collect(Collectors.toMap(ConvFieldType::getClientDataType, ConvFieldType::getClientFieldType));
        List<ConvFieldType> addList = new ArrayList<>();
        for (Map.Entry<String, String> fieldType : fieldMap.entrySet()){
            String dataType = fieldType.getKey();
            String fieldName = fieldType.getValue();
            if (existMap.containsKey(dataType) && existMap.get(dataType).equals(fieldName)){
                continue;
            }
            ConvFieldType type = ConvFieldType.builder()
                    .clientSource(datasourceConfig.getDbType())
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
}
