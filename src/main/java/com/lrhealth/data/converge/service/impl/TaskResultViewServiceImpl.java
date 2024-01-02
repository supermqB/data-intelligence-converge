package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.dao.entity.ConvTaskResultView;
import com.lrhealth.data.converge.dao.service.ConvTaskResultViewService;
import com.lrhealth.data.converge.model.dto.ResultRecordDto;
import com.lrhealth.data.converge.service.TaskResultViewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author jinmengyu
 * @date 2024-01-02
 */
@Service
public class TaskResultViewServiceImpl implements TaskResultViewService {
    @Resource
    private ConvTaskResultViewService convTaskResultViewService;

    @Override
    @Transactional
    public void updateTaskResultViewCount(ResultRecordDto recordDto) {
        ConvTaskResultView taskResultView = convTaskResultViewService.getOne(new LambdaQueryWrapper<ConvTaskResultView>()
                .eq(ConvTaskResultView::getTaskId, recordDto.getTaskId())
                .eq(ConvTaskResultView::getTableName, recordDto.getTableName()));
        if (ObjectUtil.isEmpty(taskResultView)){
            return;
        }
        convTaskResultViewService.updateById(ConvTaskResultView.builder()
                .id(taskResultView.getId())
                .dataItemCount(recordDto.getCount()).build());
    }
}
