package com.lrhealth.data.converge.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.enums.ProbeModelEnum;
import com.lrhealth.data.converge.dao.entity.ConvOriginalProbe;
import com.lrhealth.data.converge.dao.mapper.ConvOriginalProbeMapper;
import com.lrhealth.data.converge.dao.service.ConvOriginalProbeService;
import org.springframework.stereotype.Service;

/**
 * @author jinmengyu
 * @date 2025-04-14
 */
@Service
public class ConvOriginalProbeServiceImpl extends ServiceImpl<ConvOriginalProbeMapper, ConvOriginalProbe> implements ConvOriginalProbeService {
    @Override
    public void deletePastDictValue(Long columnId, Long tableId) {
        boolean isDelete =  this.remove(new LambdaQueryWrapper<ConvOriginalProbe>()
                .eq(ConvOriginalProbe::getTableId, tableId)
                .eq(ConvOriginalProbe::getColumnId, columnId)
                .eq(ConvOriginalProbe::getProbeModel, ProbeModelEnum.COLUMN_VALUEFREQ.getCode()));
        if (!isDelete){
            throw new CommonException("删除已有的值域数据失败，columnId=" + columnId);
        }
    }
}
