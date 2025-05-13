package com.lrhealth.data.converge.dao.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.dao.entity.ConvTaskResultCdc;
import com.lrhealth.data.converge.dao.mapper.ConvTaskResultCdcMapper;
import com.lrhealth.data.converge.dao.service.ConvTaskResultCdcService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhangguowen-generator
 * @since 2023-09-18
 */
@Service
public class ConvTaskResultCdcServiceImpl extends ServiceImpl<ConvTaskResultCdcMapper, ConvTaskResultCdc> implements ConvTaskResultCdcService {

    @Override
    public void insertOrUpdateTaskResultCdc(ConvTaskResultCdc taskResultCdc) {
        List<ConvTaskResultCdc> resultCdcs = this.list(new LambdaQueryWrapper<ConvTaskResultCdc>()
                .eq(ConvTaskResultCdc::getTaskId, taskResultCdc.getTaskId())
                .eq(ConvTaskResultCdc::getTableName, taskResultCdc.getTableName()));
        if (CollUtil.isNotEmpty(resultCdcs)){
            ConvTaskResultCdc convTaskResultCdc = resultCdcs.get(0);
            convTaskResultCdc.setDataCount(taskResultCdc.getDataCount() + convTaskResultCdc.getDataCount());
            convTaskResultCdc.setAddCount(taskResultCdc.getAddCount() + convTaskResultCdc.getAddCount());
            convTaskResultCdc.setUpdateCount(taskResultCdc.getUpdateCount() + convTaskResultCdc.getUpdateCount());
            convTaskResultCdc.setDeleteCount(taskResultCdc.getDeleteCount() + convTaskResultCdc.getDeleteCount());
            this.updateById(convTaskResultCdc);
        }else {
            this.save(taskResultCdc);
        }
    }
}
