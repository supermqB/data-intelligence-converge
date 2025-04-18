package com.lrhealth.data.converge.dao.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.ConvOriginalTable;
import com.lrhealth.data.converge.dao.mapper.ConvOriginalTableMapper;
import com.lrhealth.data.converge.dao.service.ConvOriginalTableService;
import com.lrhealth.data.converge.model.dto.OriginalTableModelDto;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 客户端库表信息 服务实现类
 * </p>
 *
 * @author jinmengyu
 * @since 2024-01-03
 */
@Service
public class ConvOriginalTableServiceImpl extends ServiceImpl<ConvOriginalTableMapper, ConvOriginalTable> implements ConvOriginalTableService {

    @Override
    public OriginalTableModelDto getTableModelRel(String oriTableName, String systemCode) {
        if (CharSequenceUtil.isBlank(oriTableName) || CharSequenceUtil.isBlank(systemCode)){
            throw new CommonException("oriTableName or systemCode is null!");
        }
        return this.baseMapper.getTableModelRel(oriTableName, systemCode);
    }
}
