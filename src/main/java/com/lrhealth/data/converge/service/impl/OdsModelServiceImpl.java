package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.StdOriginalModel;
import com.lrhealth.data.converge.dao.entity.StdOriginalModelColumn;
import com.lrhealth.data.converge.dao.service.StdOriginalModelColumnService;
import com.lrhealth.data.converge.dao.service.StdOriginalModelService;
import com.lrhealth.data.converge.service.OdsModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author jinmengyu
 * @date 2023-08-21
 */
@Service
@Slf4j
public class OdsModelServiceImpl implements OdsModelService {

    @Resource
    private StdOriginalModelService originalModelService;
    @Resource
    private StdOriginalModelColumnService modelColumnService;

    @Override
    public String getTableDataType(String odsTableName, String sysCode) {
        List<StdOriginalModel> tableList = originalModelService.list(new LambdaQueryWrapper<StdOriginalModel>()
                .eq(CharSequenceUtil.isNotBlank(odsTableName), StdOriginalModel::getNameEn, odsTableName)
                .eq(CharSequenceUtil.isNotBlank(sysCode), StdOriginalModel::getSysCode, sysCode)
                .eq(StdOriginalModel::getDelFlag, 0));
        if (tableList.size() > 1){
            throw new CommonException("originalModel查询{}错误", odsTableName);
        }
        if (CollUtil.isEmpty(tableList)){
            return null;
        }
        return tableList.get(0).getDataType();
    }
    @Override
    public List<StdOriginalModelColumn> getColumnList(String odsModelName, String sysCode) {
        List<StdOriginalModel> originalModel = originalModelService.list(new LambdaQueryWrapper<StdOriginalModel>()
                .eq(StdOriginalModel::getNameEn, odsModelName)
                .eq(StdOriginalModel::getSysCode, sysCode)
                .eq(StdOriginalModel::getDelFlag, 0));
        if (originalModel.size() != 1){
            throw new CommonException("原始模型数据错误:{}", odsModelName + "-" + sysCode);
        }
        return modelColumnService.list(new LambdaQueryWrapper<StdOriginalModelColumn>()
                .eq(StdOriginalModelColumn::getModelId, originalModel.get(0).getId())
                .eq(StdOriginalModelColumn::getDelFlag, 0));
    }

    @Override
    public StdOriginalModel getModel(Long modelId) {
       return originalModelService.list(new LambdaQueryWrapper<StdOriginalModel>()
               .eq(StdOriginalModel::getId,modelId)
                .eq(StdOriginalModel::getDelFlag,0)).get(0);
    }

}
