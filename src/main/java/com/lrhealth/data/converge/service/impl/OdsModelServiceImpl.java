package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.service.OdsModelService;
import com.lrhealth.data.model.original.model.OriginalModel;
import com.lrhealth.data.model.original.model.OriginalModelColumn;
import com.lrhealth.data.model.original.service.OriginalModelColumnService;
import com.lrhealth.data.model.original.service.OriginalModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jinmengyu
 * @date 2023-08-21
 */
@Service
@Slf4j
public class OdsModelServiceImpl implements OdsModelService {

    @Resource
    private OriginalModelService originalModelService;
    @Resource
    private OriginalModelColumnService modelColumnService;

    @Override
    public String getTableDataType(String odsTableName, String sysCode) {
        List<OriginalModel> tableList = originalModelService.list(new LambdaQueryWrapper<OriginalModel>().eq(CharSequenceUtil.isNotBlank(odsTableName), OriginalModel::getNameEn, odsTableName)
                .eq(CharSequenceUtil.isNotBlank(sysCode), OriginalModel::getSysCode, sysCode));
        if (tableList.size() != 1){
            throw new CommonException("originalModel查询{}错误", odsTableName);
        }
        return tableList.get(0).getDataType();
    }

    @Override
    public Map<String, String> getOdsColumnTypeMap(String odsModelName, String sysCode) {
        List<OriginalModel> modelList = originalModelService.list(new LambdaQueryWrapper<OriginalModel>()
                .eq(OriginalModel::getNameEn, odsModelName)
                .eq(OriginalModel::getSysCode, sysCode));
        if (modelList.size() != 1){
            throw new CommonException("originalModel查询{}错误", odsModelName);
        }
        List<OriginalModelColumn> modelColumns = modelColumnService.list(new LambdaQueryWrapper<OriginalModelColumn>()
                .eq(OriginalModelColumn::getModelId, modelList.get(0).getId()));
        return modelColumns.stream().collect(Collectors.toMap(OriginalModelColumn::getNameEn, OriginalModelColumn::getElementFormat));
    }

    @Override
    public List<OriginalModelColumn> getcolumnList(String odsModelName, String sysCode) {
        List<OriginalModel> originalModel = originalModelService.list(new LambdaQueryWrapper<OriginalModel>()
                .eq(OriginalModel::getNameEn, odsModelName).eq(OriginalModel::getSysCode, sysCode));
        if (originalModel.size() != 1){
            throw new CommonException("原始模型数据错误:{}", odsModelName + "-" + sysCode);
        }
        return modelColumnService.list(new LambdaQueryWrapper<OriginalModelColumn>()
                .eq(OriginalModelColumn::getModelId, originalModel.get(0).getId()));
    }
}
