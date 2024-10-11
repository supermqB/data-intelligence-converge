package com.lrhealth.data.converge.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.dao.entity.ConvFepErrorLog;
import com.lrhealth.data.converge.dao.mapper.ConvFepErrorLogMapper;
import com.lrhealth.data.converge.dao.service.ConvFepErrorLogService;
import com.lrhealth.data.converge.model.dto.FepErrorDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @Author jinmengyu
 * @Date: 2024/10/10/10:33
 */
@Service
public class ConvFepErrorLogServiceImpl extends ServiceImpl<ConvFepErrorLogMapper, ConvFepErrorLog> implements ConvFepErrorLogService {
    @Override
    public void saveErrorLog(FepErrorDto dto) {
        ConvFepErrorLog errorLog  = ConvFepErrorLog.builder()
                .ip(dto.getIp())
                .port(Integer.valueOf(dto.getPort()))
                .errorMsg(dto.getErrorMsg())
                .stacktrace(dto.getStacktrace())
                .logTime(LocalDateTime.now())
                .orgCode(dto.getOrgCode())
                .build();
        this.save(errorLog);
    }
}
