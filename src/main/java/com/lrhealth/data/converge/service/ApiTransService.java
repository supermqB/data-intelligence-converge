package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import java.util.Map;

/**
 * @author zhuanning
 * @date 2023-11-01
 */
public interface ApiTransService {
    /**
     * 数据写入
     * @param subject 数据源
     * @param paramMap 参数
     * @return tunnel
     */
    ConvTunnel upload(String subject, Map<String, Object> paramMap);
}
