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
     * @param convTunnel 汇聚管道
     * @param paramMap 参数
     * @return tunnel
     */
    boolean upload(ConvTunnel convTunnel, Map<String, Object> paramMap);
}
