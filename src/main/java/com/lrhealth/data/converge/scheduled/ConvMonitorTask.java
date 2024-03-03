package com.lrhealth.data.converge.scheduled;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.util.QueryParserUtil;
import com.lrhealth.data.converge.dao.entity.ConvOdsDatasourceConfig;
import com.lrhealth.data.converge.dao.mapper.ConvOdsDatasourceConfigMapper;
import com.lrhealth.data.converge.dao.service.ConvMonitorService;
import com.lrhealth.data.converge.database.DatabaseFactory;
import com.lrhealth.data.converge.database.DatabaseHandler;
import com.lrhealth.data.converge.model.dto.MonitorMsg;
import com.lrhealth.data.converge.model.vo.DbValidVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 汇聚监测信息定时任务
 *
 * @author admin
 */
@Slf4j
@Component
@EnableScheduling
public class ConvMonitorTask implements CommandLineRunner {

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${spring.kafka.topic.fep.mirror-db-get}")
    private String incrDataTopic;
    @Resource
    private ConvOdsDatasourceConfigMapper convOdsDatasourceConfigMapper;
    @Resource
    private ConvMonitorService convMonitorService;
    @Value("${region.current.code}")
    private String regionCode;

    private static final String INCREMENT_TYPE_KEY = "INCREMENT_TYPE_KEY";

    private static final String MIRROR_DB_TYPE_KEY = "MIRROR_DB_TYPE_KEY";
    @Override
    public void run(String... args) throws Exception {
        monitorIncrementTask();
        targetDataBaseMonitorTask();
        mirrorDataBaseMonitorTask();
    }

    /**
     * 定时发送增量数据监测状态
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void monitorIncrementTask() {
        List<ConvOdsDatasourceConfig> mirrorDbList = getDataSourceConfigByType((short) 2);
        if (CollectionUtils.isEmpty(mirrorDbList)) {
            return;
        }
        kafkaTemplate.send(incrDataTopic,INCREMENT_TYPE_KEY, JSON.toJSONString(mirrorDbList));
    }

    /**
     * 镜像库连接状态监控
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void mirrorDataBaseMonitorTask() {
        List<ConvOdsDatasourceConfig> mirrorDbList = getDataSourceConfigByType((short) 2);
        if (CollectionUtils.isEmpty(mirrorDbList)) {
            return;
        }
        kafkaTemplate.send(incrDataTopic,MIRROR_DB_TYPE_KEY, JSON.toJSONString(mirrorDbList));
    }

    private List<ConvOdsDatasourceConfig> getDataSourceConfigByType(short dsType){
         List<ConvOdsDatasourceConfig> datasourceConfigs = convOdsDatasourceConfigMapper.selectList(new LambdaQueryWrapper<ConvOdsDatasourceConfig>().eq(ConvOdsDatasourceConfig::getDelFlag, 0)
                .eq(ConvOdsDatasourceConfig::getDsType, dsType));
         if (CollectionUtils.isNotEmpty(datasourceConfigs)){
             return datasourceConfigs.stream().filter(dsConfig -> regionCode.equals(dsConfig.getOrgCode().substring(1, regionCode.length() + 1))).collect(Collectors.toList());
         }
         return null;
    }

    /**
     * 目标库连接状态监控
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void targetDataBaseMonitorTask() {
        final List<ConvOdsDatasourceConfig> targetDbList = getDataSourceConfigByType((short) 1);
        if (CollectionUtils.isEmpty(targetDbList)) {
            return;
        }
        targetDbList.forEach(dsConf -> {
            dbLinkAndHttpPost(MonitorMsg.MsgTypeEnum.WRITER_DB_CHECK,dsConf);
        });
    }

    public void dbLinkAndHttpPost(MonitorMsg.MsgTypeEnum msgTypeEnum, ConvOdsDatasourceConfig dsConfig) {
        DbValidVo dbMessage = QueryParserUtil.getDbMessage(dsConfig.getDsUrl());
        if (ObjectUtil.isEmpty(dbMessage)) {
            return;
        }
        try {
            DatabaseHandler reader = new DatabaseFactory().getReader(dsConfig.getDsUrl(), dsConfig.getDsUsername(), dsConfig.getDsPwd());
            // 获取目标库连接状态
            boolean dbLink = reader.testConnection();
            MonitorMsg monitorMsg = buildMonitorMsg(dsConfig, msgTypeEnum, dbLink);
            //处理监控消息
            convMonitorService.handleMonitorMsg(monitorMsg);
        } catch (Exception e) {
            log.error("database check error: {}", ExceptionUtils.getStackTrace(e));
        }
    }

    private MonitorMsg buildMonitorMsg(ConvOdsDatasourceConfig dsConfig,MonitorMsg.MsgTypeEnum msgTypeEnum,boolean dbLink){
        MonitorMsg monitorMsg = MonitorMsg.builder()
                .sourceIp(dsConfig.getDbIp())
                .sourcePort(dsConfig.getDbPort())
                .msgType(msgTypeEnum.getMsgTypeCode())
                .status(dbLink)
                .tableNames(null)
                .orgCode(dsConfig.getOrgCode())
                .tunnelId(null)
                .dsId(dsConfig.getId())
                .sendTime(DateUtil.date())
                .build();
        if (dbLink) {
            monitorMsg.setMsg(null);
        } else {
            monitorMsg.setMsg("目标库连接异常");
        }
        return monitorMsg;
    }

}
