package com.lrhealth.data.converge.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lrhealth.data.converge.dao.entity.ConvOriginalProbe;

/**
 * @author jinmengyu
 * @date 2025-04-14
 */
public interface ConvOriginalProbeService extends IService<ConvOriginalProbe> {

    void deletePastDictValue(Long columnId, Long tableId);
}
