package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.Xds;

/**
 * @author jinmengyu
 * @date 2023-10-12
 */
public interface KafkaService {

    void xdsSendKafka(Xds xds);
}
