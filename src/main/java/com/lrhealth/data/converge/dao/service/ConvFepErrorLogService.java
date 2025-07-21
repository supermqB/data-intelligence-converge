package com.lrhealth.data.converge.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lrhealth.data.converge.dao.entity.ConvFepErrorLog;
import com.lrhealth.data.converge.model.dto.FepErrorDto;

/**
 * @author jinmengyu
 * @date: 2024/10/10/10:33
 */
public interface ConvFepErrorLogService extends IService<ConvFepErrorLog> {

    void saveErrorLog(FepErrorDto dto);
}
