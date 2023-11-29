package com.lrhealth.data.converge.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson2.JSON;
import com.lrhealth.data.converge.model.dto.CdcRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * CDC 汇聚数据生产者
 * </p>
 *
 * @author lr
 * @since 2023/11/28 22:14
 */
@Component
public class CdcConvDataProduct {
    @Value("${spring.kafka.topic.cdc}")
    private String cdcConvDataTopic;
    @Resource(name = "kafkaTemplate")
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    public void send(List<CdcRecord> cdcRecordList) {
        if (CollUtil.isEmpty(cdcRecordList)) {
            return;
        }
        kafkaTemplate.send(cdcConvDataTopic, JSON.toJSONString(cdcRecordList));
        ThreadUtil.sleep(20,TimeUnit.SECONDS);
    }
}
