package com.lrhealth.data.converge.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lrhealth.data.converge.dao.entity.ConvCollectField;

import java.util.List;

/**
 * @Author lei
 * @Date: 2023/11/30/18:32
 */
public interface ConvCollectFieldService extends IService<ConvCollectField> {

    List<ConvCollectField> getTunnelTableConfigs(Long tunnelId);
}
