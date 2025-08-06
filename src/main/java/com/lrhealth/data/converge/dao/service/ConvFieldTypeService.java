package com.lrhealth.data.converge.dao.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lrhealth.data.converge.dao.entity.ConvFieldType;
import com.lrhealth.data.converge.model.dto.DbTypeDto;

/**
 * <p>
 * 数据源字段类型映射
 * </p>
 *
 * @author flzhang
 * @since 2024-01-23
 */
public interface ConvFieldTypeService extends IService<ConvFieldType> {

    void saveFieldType(List<DbTypeDto> fieldList, Integer convDsConfigId);

    void saveFieldType(DbTypeDto field, Integer convDsConfigId);

    void saveFieldType(List<DbTypeDto> fieldList, String dbType);

    String getFormatElement(String fieldType, Integer fieldTypeLength);

    Map<String, String> getFieldTypeMappingBySrc(String srcDbType, String tgtDbType);
}
