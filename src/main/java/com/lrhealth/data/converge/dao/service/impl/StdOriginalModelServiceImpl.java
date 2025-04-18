package com.lrhealth.data.converge.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.dao.entity.StdOriginalModel;
import com.lrhealth.data.converge.dao.entity.StdOriginalModelColumn;
import com.lrhealth.data.converge.dao.mapper.StdOriginalModelMapper;
import com.lrhealth.data.converge.dao.service.StdOriginalModelService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 原始模型字段信息 服务实现类
 * </p>
 *
 * @author lr
 * @since 2023-08-09
 */
@Service
public class StdOriginalModelServiceImpl extends ServiceImpl<StdOriginalModelMapper, StdOriginalModel> implements StdOriginalModelService {

    @Override
    public List<StdOriginalModelColumn> queryModelAndColumnByCatalogId() {
        return this.baseMapper.queryModelAndColumnByCatalogId();
    }
}
