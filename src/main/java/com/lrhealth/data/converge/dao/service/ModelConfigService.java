package com.lrhealth.data.converge.dao.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.lrhealth.data.converge.dao.entity.ModelConfig;


/**
 * @author admin
 */
public interface ModelConfigService extends IService<ModelConfig> {

   ModelConfig getModelConfig(Long modelId);

}
