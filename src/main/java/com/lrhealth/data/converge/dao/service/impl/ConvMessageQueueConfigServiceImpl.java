package com.lrhealth.data.converge.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.dao.entity.ConvMessageQueueConfig;
import com.lrhealth.data.converge.dao.mapper.ConvMessageQueueConfigMapper;
import com.lrhealth.data.converge.dao.service.ConvMessageQueueConfigService;
import org.springframework.stereotype.Service;

/**
 * @Author lei
 * @Date: 2023/11/30/18:33
 */
@Service
public class ConvMessageQueueConfigServiceImpl extends ServiceImpl<ConvMessageQueueConfigMapper, ConvMessageQueueConfig> implements ConvMessageQueueConfigService {
}
