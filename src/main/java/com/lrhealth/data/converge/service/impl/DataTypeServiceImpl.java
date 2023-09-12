package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.service.DataTypeService;
import com.lrhealth.data.model.original.model.OriginalModel;
import com.lrhealth.data.model.original.service.OriginalModelService;
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
public class DataTypeServiceImpl implements DataTypeService {

    @Resource
    private OriginalModelService originalModelService;
    @Override
    public String getTableDataType(String odsTableName, String sysCode) {
        List<OriginalModel> tableList = originalModelService.list(new LambdaQueryWrapper<OriginalModel>().eq(CharSequenceUtil.isNotBlank(odsTableName), OriginalModel::getNameEn, odsTableName)
                .eq(CharSequenceUtil.isNotBlank(sysCode), OriginalModel::getSysCode, sysCode));
        if (tableList.size() != 1){
            throw new CommonException("originalModel查询{}错误", odsTableName);
        }
        return tableList.get(0).getDataType();
    }
}
