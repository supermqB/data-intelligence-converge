package com.lrhealth.data.converge.scheduled.dao.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultView;
import com.lrhealth.data.converge.scheduled.dao.mapper.DiConvTaskResultViewMapper;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskResultViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhangguowen-generator
 * @since 2023-09-18
 */

@Slf4j
@Service
public class ConvTaskResultViewServiceImpl extends ServiceImpl<DiConvTaskResultViewMapper, ConvTaskResultView> implements ConvTaskResultViewService {


    @Override
    public ConvTaskResultView createJobExecInstance(Integer taskId, Long dataxConfigId, String tableName, String startIndex, String endIndex) {
        List<ConvTaskResultView> jobInstanceList = this.list(new LambdaQueryWrapper<ConvTaskResultView>()
                .eq(ConvTaskResultView::getTaskId, taskId)
                .eq(ConvTaskResultView::getDataxJobId, dataxConfigId));
        if (ObjectUtil.isNotEmpty(jobInstanceList)){
            this.removeBatchByIds(jobInstanceList);
        }
        ConvTaskResultView resultView = ConvTaskResultView.builder()
                .taskId(taskId).dataxConfigId(dataxConfigId).tableName(tableName)
                .status(1).createTime(LocalDateTime.now())
                .delFlag(0)
                .build();
        if (!"null".equals(startIndex) && null != startIndex){
            log.info("(startIndex) create startIndex: {}", startIndex);
            resultView.setStartIndex(Integer.valueOf(startIndex));
        }
        if (!"null".equals(startIndex) && null != startIndex){
            log.info("(endIndex) create endIndex: {}", endIndex);
            resultView.setEndIndex(Integer.valueOf(endIndex));
        }
        this.save(resultView);
        return resultView;
    }

    @Override
    public Long getDataXJoId(Integer taskResultViewId, Integer taskId) {
        String suffix;
        if (taskResultViewId >= 100) {
            String jobId = String.valueOf(taskResultViewId);
            suffix = String.valueOf(taskResultViewId).substring(jobId.length() - 2);
        }else if (taskResultViewId >= 10){
            suffix = String.valueOf(taskResultViewId);
        }else {
            suffix = taskResultViewId + "0";
        }
        Long jobId = Long.valueOf((taskId * 100) + suffix);
        this.updateById(ConvTaskResultView.builder().id(taskResultViewId).dataxJobId(jobId).build());
        return jobId;
    }
}
