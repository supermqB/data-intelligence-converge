package com.lrhealth.data.converge.kafka;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.lrhealth.data.converge.dao.service.ConvFepErrorLogService;
import com.lrhealth.data.converge.dao.service.ConvMonitorService;
import com.lrhealth.data.converge.model.dto.*;
import com.lrhealth.data.converge.service.FeTunnelConfigService;
import com.lrhealth.data.converge.service.ImportOriginalService;
import com.lrhealth.data.converge.service.TaskResultViewService;
import com.lrhealth.data.converge.service.XdsInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author jinmengyu
 * @date 2024-02-04
 */
@Slf4j
@Component
public class FepKafkaConsumer {

    @Resource
    private XdsInfoService xdsInfoService;
    @Resource
    private TaskResultViewService taskResultViewService;
    @Resource
    private FeTunnelConfigService feTunnelConfigService;
    @Resource
    private ImportOriginalService importOriginalService;
    @Resource
    private ConvMonitorService convMonitorService;
    @Resource
    private ConvFepErrorLogService fepErrorLogService;


    @KafkaListener(topics = "${spring.kafka.topic.fep.update-collect-message}")
    public void updateCollectInfo(ConsumerRecord<String, String> record, Acknowledgment acknowledgment){
        log.info("====================receive update-fepStatus msgBody={}", record);
        try {
            String key = record.key();
            String msgBody = record.value();
            feTunnelConfigService.kafkaUpdateFepStatus(key, msgBody);
        } catch (Exception e) {
            log.error("fepStatus update error,{}", ExceptionUtils.getStackTrace(e));
        } finally {
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(topics = "${spring.kafka.topic.fep.xds-create}")
    public Boolean fepCreateXds(@Payload String msgBody, Acknowledgment acknowledgment){
        log.info("====================receive xds-create msgBody={}", msgBody);
        Boolean createResult = false;
        try {
            DbXdsMessageDto dbXdsMessageDto = JSON.parseObject(msgBody, DbXdsMessageDto.class);
            createResult =  xdsInfoService.fepCreateXds(dbXdsMessageDto);
        } catch (Exception e) {
            log.error("XdsCreate error,{}", ExceptionUtils.getStackTrace(e));
        } finally {
            acknowledgment.acknowledge();
        }
        return createResult;
    }

    @KafkaListener(topics = "${spring.kafka.topic.fep.xds-update}")
    public Boolean fepUpdateXds(@Payload String msgBody, Acknowledgment acknowledgment){
        log.info("====================receive xds-update-msgBody = {}", msgBody);
        Boolean updateResult = false;
        try {
            DbXdsMessageDto dbXdsMessageDto = JSON.parseObject(msgBody, DbXdsMessageDto.class);
            updateResult = xdsInfoService.fepUpdateXds(dbXdsMessageDto);
        } catch (Exception e) {
            log.error("XdsUpdate error,{}", ExceptionUtils.getStackTrace(e));
        } finally {
            acknowledgment.acknowledge();
        }
        return updateResult;
    }

    @KafkaListener(topics = "${spring.kafka.topic.fep.upload-tableCount}")
    public void uploadTaskResultViewCount(@Payload String msgBody, Acknowledgment acknowledgment){
        log.info("====================receive taskResultView-upload msgBody={}", msgBody);
        try {
            ResultRecordDto recordDto = JSON.parseObject(msgBody, ResultRecordDto.class);
            taskResultViewService.updateTaskResultViewCount(recordDto);
        } catch (Exception e) {
            log.error("TaskResultView count update error,{}", ExceptionUtils.getStackTrace(e));
        } finally {
            acknowledgment.acknowledge();
        }
    }


    @KafkaListener(topics = "${spring.kafka.topic.fep.upload-original-structure}")
    public void uploadStructure(@Payload String msgBody, Acknowledgment acknowledgment){
        try {
            OriginalStructureDto structureDto = JSON.parseObject(msgBody, OriginalStructureDto.class);
            log.info("====================receive upload originalStructure dsId = {},orgCode ={},sysCode = {},tableSize = {}",structureDto.getDsConfId(),structureDto.getOrgCode(),structureDto.getSysCode(), CollectionUtil.isEmpty(structureDto.getOriginalTables()) ? 0 : structureDto.getOriginalTables().size());
            importOriginalService.importConvOriginal(structureDto);
        } catch (Exception e) {
            log.error("upload originalStructure error,{}", ExceptionUtils.getStackTrace(e));
        } finally {
            acknowledgment.acknowledge();
        }
    }


    @KafkaListener(topics = "${spring.kafka.topic.fep.upload-original-table-count}")
    public void uploadTableCount(@Payload String msgBody, Acknowledgment acknowledgment){
        log.info("====================receive upload originalTable count msgBody={}", msgBody);
        try {
            OriginalTableCountDto tableCountDto = JSON.parseObject(msgBody, OriginalTableCountDto.class);
            importOriginalService.updateOriginalTableCount(tableCountDto);
        } catch (Exception e) {
            log.error("upload originalTable count error,{}", ExceptionUtils.getStackTrace(e));
        } finally {
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(topics = "${spring.kafka.topic.fep.monitor-msg}")
    public void uploadMonitorMsg(ConsumerRecord<String, String> monitorRecord, Acknowledgment acknowledgment){
        String msgBody = monitorRecord.value();
        log.info("====================receive upload monitor message msgBody={}", msgBody);
        try {
            String key = monitorRecord.key();
            if ("FEP_ERROR".equals(key)) {
                FepErrorDto errorDto = JSON.parseObject(msgBody, FepErrorDto.class);
                fepErrorLogService.saveErrorLog(errorDto);
                return;
            }
            MonitorMsg monitorMsg = JSON.parseObject(msgBody, MonitorMsg.class);
            convMonitorService.handleMonitorMsg(monitorMsg);
        } catch (Exception e) {
            log.error("upload originalTable count error,{}", ExceptionUtils.getStackTrace(e));
        } finally {
            acknowledgment.acknowledge();
        }
    }

}
