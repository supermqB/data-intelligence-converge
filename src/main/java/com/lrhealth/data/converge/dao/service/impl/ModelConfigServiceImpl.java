package com.lrhealth.data.converge.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.dao.entity.ModelConfig;
import com.lrhealth.data.converge.dao.mapper.ModelConfigMapper;
import com.lrhealth.data.converge.dao.service.ModelConfigService;
import org.springframework.stereotype.Service;

@Service
public class ModelConfigServiceImpl extends ServiceImpl<ModelConfigMapper, ModelConfig> implements ModelConfigService {
}
