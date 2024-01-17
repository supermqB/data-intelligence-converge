package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.util.SqlExecUtil;
import com.lrhealth.data.converge.dao.entity.*;
import com.lrhealth.data.converge.dao.service.*;
import com.lrhealth.data.converge.model.dto.DataSourceDto;
import com.lrhealth.data.converge.service.IncrTimeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author jinmengyu
 * @date 2024-01-17
 */
@Service
public class IncrTimeServiceImpl implements IncrTimeService {

    @Resource
    private ConvTaskService taskService;
    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private ConvOdsDatasourceConfigService datasourceConfigService;
    @Resource
    private ConvOriginalTableService originalTableService;
    @Resource
    private ConvOriginalColumnService originalColumnService;
    @Resource
    private ConvCollectIncrTimeService convCollectIncrTimeService;

    @Override
    public void updateTableLatestTime(Xds xds) {
        ConvTask convTask = taskService.getById(xds.getConvTaskId());
        if (ObjectUtil.isNull(convTask)){
            return;
        }
        ConvTunnel tunnel = tunnelService.getById(convTask.getTunnelId());
        ConvOdsDatasourceConfig dsConfig = datasourceConfigService.getById(xds.getDsConfigId());
        DataSourceDto dto = DataSourceDto.builder()
                .driver(dsConfig.getDsDriverName())
                .jdbcUrl(dsConfig.getDsUrl())
                .username(dsConfig.getDsUsername())
                .password(dsConfig.getDsPwd()).build();
        ConvOriginalTable table = originalTableService.getOne(new LambdaQueryWrapper<ConvOriginalTable>()
                .eq(ConvOriginalTable::getConvDsConfId, tunnel.getReaderDatasourceId())
                .eq(ConvOriginalTable::getNameEn, xds.getOdsModelName()));
        if (ObjectUtil.isEmpty(table)){
            return;
        }
        List<ConvOriginalColumn> convOriginalColumns = originalColumnService.list(new LambdaQueryWrapper<ConvOriginalColumn>()
                .eq(ConvOriginalColumn::getTableId, table.getId())
                .eq(ConvOriginalColumn::getIncrFlag, "1"));
        if (CollUtil.isEmpty(convOriginalColumns)){
            return;
        }
        convOriginalColumns.forEach(column -> {
            String sql = getTaskLatestTimeSql(xds.getId(), xds.getOdsTableName(), column.getNameEn());
            List<Map<String, Object>> mapList = SqlExecUtil.execSql(sql, dto);
            Object latestTime = mapList.get(0).get(column.getNameEn());
            updateCollectIncrTime(tunnel, xds, column.getNameEn(), latestTime);
        });
    }

    private void updateCollectIncrTime(ConvTunnel tunnel, Xds xds, String column, Object latestTime){
        ConvCollectIncrTime collectIncrTime = convCollectIncrTimeService.getOne(new LambdaQueryWrapper<ConvCollectIncrTime>()
                .eq(ConvCollectIncrTime::getTunnelId, tunnel.getId())
                .eq(ConvCollectIncrTime::getTableName, xds.getOdsTableName())
                .eq(ConvCollectIncrTime::getIncrField, column));
        if (ObjectUtil.isNotNull(collectIncrTime)) {
            ConvCollectIncrTime update = ConvCollectIncrTime.builder()
                    .id(collectIncrTime.getId())
                    .latestTime(latestTime.toString())
                    .updateTime(LocalDateTime.now()).build();
            convCollectIncrTimeService.updateById(update);
        }
        ConvCollectIncrTime build = ConvCollectIncrTime.builder()
                .tunnelId(tunnel.getId())
                .tableName(xds.getOdsTableName())
                .incrField(column)
                .latestTime(latestTime.toString())
                .createTime(LocalDateTime.now())
                .orgCode(xds.getOrgCode())
                .sysCode(xds.getSysCode())
                .build();
        convCollectIncrTimeService.saveOrUpdate(build);
    }




    private String getTaskLatestTimeSql(Long xdsId, String tableName, String businessField){
        return "SELECT " + businessField + " FROM " + tableName +
                " WHERE xds_id = '" + xdsId + "' ORDER BY " + businessField + " LIMIT 1";
    }
}
