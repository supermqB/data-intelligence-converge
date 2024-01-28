package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.dto.DsKafkaDto;

/**
 * @author jinmengyu
 * @date 2023-10-12
 */
public interface KafkaService {

    void xdsSendKafka(Xds xds);

    void dsSendKafka(DsKafkaDto dto);
}
