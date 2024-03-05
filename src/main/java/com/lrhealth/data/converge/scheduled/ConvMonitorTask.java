package com.lrhealth.data.converge.scheduled;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.util.QueryParserUtil;
import com.lrhealth.data.converge.dao.entity.ConvOdsDatasourceConfig;
import com.lrhealth.data.converge.dao.service.ConvMonitorService;
import com.lrhealth.data.converge.dao.service.ConvOdsDatasourceConfigService;
import com.lrhealth.data.converge.database.DatabaseFactory;
import com.lrhealth.data.converge.database.DatabaseHandler;
import com.lrhealth.data.converge.model.dto.MonitorMsg;
import com.lrhealth.data.converge.model.vo.DbValidVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

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
    private ConvOdsDatasourceConfigService convOdsDatasourceConfigService;
    @Resource
    private ConvMonitorService convMonitorService;
    @Override
    public void run(String... args) throws Exception {
        targetDataBaseMonitorTask();
    }

    /**
     * 目标库状态监测
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void targetDataBaseMonitorTask() {
        List<ConvOdsDatasourceConfig> targetDbConfList = convOdsDatasourceConfigService.list(new LambdaQueryWrapper<ConvOdsDatasourceConfig>()
                .eq(ConvOdsDatasourceConfig::getDsType, 1).eq(ConvOdsDatasourceConfig::getDelFlag, 0));
        if (CollectionUtils.isNotEmpty(targetDbConfList)){
            return;
        }
        for (ConvOdsDatasourceConfig dsConf : targetDbConfList) {
            dbLinkAndHttpPost(MonitorMsg.MsgTypeEnum.TARGET_DB_CHECK,dsConf);
        }
    }

    public void dbLinkAndHttpPost(MonitorMsg.MsgTypeEnum msgTypeEnum, ConvOdsDatasourceConfig dsConfig) {
        DbValidVo dbMessage = QueryParserUtil.getDbMessage(dsConfig.getDsUrl());
        if (ObjectUtil.isEmpty(dbMessage)) {
            return;
        }
        try {
            DatabaseHandler reader = new DatabaseFactory().getReader(dsConfig.getDsUrl(), dsConfig.getDsUsername(), dsConfig.getDsPwd());
            boolean dbLink = reader.testConnection();
            MonitorMsg monitorMsg = buildMonitorMsg(dsConfig, msgTypeEnum, dbLink);
            convMonitorService.handleMonitorMsg(monitorMsg);
        } catch (Exception e) {
            log.error("database check error: {}", ExceptionUtils.getStackTrace(e));
        }
    }

    private MonitorMsg buildMonitorMsg(ConvOdsDatasourceConfig dsConfig,MonitorMsg.MsgTypeEnum msgTypeEnum,boolean dbLink){
        MonitorMsg monitorMsg = MonitorMsg.builder()
                .sourceIp(dsConfig.getDbIp())
                .sourcePort(dsConfig.getDbPort() + "")
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
