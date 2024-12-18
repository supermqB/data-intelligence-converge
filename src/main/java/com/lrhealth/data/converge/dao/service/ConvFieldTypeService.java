package com.lrhealth.data.converge.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lrhealth.data.converge.dao.entity.ConvFieldType;

import java.util.Map;

/**
 * <p>
 * 数据源字段类型映射
 * </p>
 *
 * @author flzhang
 * @since 2024-01-23
 */
public interface ConvFieldTypeService extends IService<ConvFieldType> {

    void saveFieldType(Map<String, String> fieldMap, Integer convDsConfigId);
}
