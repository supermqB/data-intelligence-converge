package com.lrhealth.data.converge.dao.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.ConvCollectField;
import com.lrhealth.data.converge.dao.mapper.ConvCollectFieldMapper;
import com.lrhealth.data.converge.dao.service.ConvCollectFieldService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author lei
 * @Date: 2023/11/30/18:33
 */
@Service
public class ConvCollectFieldServiceImpl extends ServiceImpl<ConvCollectFieldMapper, ConvCollectField> implements ConvCollectFieldService {

    @Override
    public List<ConvCollectField> getTunnelTableConfigs(Long tunnelId) {
        List<ConvCollectField> collectFields = this.list(new LambdaQueryWrapper<ConvCollectField>()
                .eq(ConvCollectField::getTunnelId, tunnelId));
        if (CollUtil.isEmpty(collectFields)){
            throw new CommonException("管道id=" + tunnelId + "的表采集配置为空");
        }
        return collectFields;
    }
}
