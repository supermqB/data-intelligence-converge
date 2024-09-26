package com.lrhealth.data.converge.scheduled;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.dao.entity.ConvFeNode;
import com.lrhealth.data.converge.dao.entity.ConvOdsDatasourceConfig;
import com.lrhealth.data.converge.dao.service.ConvFeNodeService;
import com.lrhealth.data.converge.dao.service.ConvOdsDatasourceConfigService;
import com.lrhealth.data.converge.model.dto.ConvDsConfDTO;
import com.lrhealth.data.converge.service.KafkaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jinmengyu
 * @date 2024-09-25
 */
@Slf4j
@Component
@EnableScheduling
public class DsConfigTask implements CommandLineRunner {

    @Resource
    private ConvOdsDatasourceConfigService dsConfigService;
    @Resource
    private ConvFeNodeService feNodeService;

    @Resource
    private KafkaService kafkaService;

    @Override
    public void run(String... args) throws Exception {
        timingDsConfigSync();
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    public void timingDsConfigSync(){
        List<ConvOdsDatasourceConfig> list = dsConfigService.list(new LambdaQueryWrapper<ConvOdsDatasourceConfig>()
                .eq(ConvOdsDatasourceConfig::getDelFlag, 0));
        List<ConvFeNode> feNodeList = feNodeService.list(new LambdaQueryWrapper<ConvFeNode>().eq(ConvFeNode::getDelFlag, 0));
        Map<String, List<ConvOdsDatasourceConfig>> groupOrgCodeMap = list.stream().collect(Collectors.groupingBy(ConvOdsDatasourceConfig::getOrgCode));
        Map<String, List<Long>> feNodeIdMap = feNodeList.stream().collect(Collectors.groupingBy(ConvFeNode::getOrgCode, Collectors.mapping(ConvFeNode::getId, Collectors.toList())));

        if (CollUtil.isEmpty(groupOrgCodeMap)){
            return;
        }
        // todo: 整理orgCode S开头的监管机构的数据源向所有机构广播

        for(Map.Entry<String, List<ConvOdsDatasourceConfig>> dsMap : groupOrgCodeMap.entrySet()){
            String orgCode = dsMap.getKey();
            List<ConvOdsDatasourceConfig> dsList = dsMap.getValue();
            List<Long> orgFeNodes = feNodeIdMap.get(orgCode);
            if (CollUtil.isEmpty(orgFeNodes) && CollUtil.isEmpty(dsList)) continue;
            for (ConvOdsDatasourceConfig dsConfig : dsList){
                ConvDsConfDTO dsConfDTO = ConvDsConfDTO.builder()
                        .id(Long.valueOf(dsConfig.getId()))
                        .dsUrl(dsConfig.getDsUrl())
                        .dsUsername(dsConfig.getDsUsername())
                        .dsPwd(dsConfig.getDsPwd())
                        .dsId(dsConfig.getId())
                        .dbType(dsConfig.getDbType())
                        .orgCode(dsConfig.getOrgCode())
                        .dsType(dsConfig.getDsType())
                        .operate("update")
                        .build();
                kafkaService.dsConfigSendFep(JSON.toJSONString(dsConfDTO), orgFeNodes);
            }
        }
    }
}
