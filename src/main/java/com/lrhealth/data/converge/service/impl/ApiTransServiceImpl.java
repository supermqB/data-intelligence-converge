package com.lrhealth.data.converge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.common.util.StringUtils;
import com.lrhealth.data.converge.dao.entity.ConvOdsDatasourceConfig;
import com.lrhealth.data.converge.dao.entity.ConvTask;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.entity.StdOriginalModel;
import com.lrhealth.data.converge.dao.mapper.StdOriginalModelMapper;
import com.lrhealth.data.converge.dao.service.ConvOdsDatasourceConfigService;
import com.lrhealth.data.converge.dao.service.ConvTaskService;
import com.lrhealth.data.converge.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.service.ApiTransService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 接口传输
 *
 * @author admin
 */
@Service
@Slf4j
public class ApiTransServiceImpl implements ApiTransService {
    @Resource
    private ConvOdsDatasourceConfigService convOdsDatasourceConfigService;
    @Resource
    private ConvTunnelService convTunnelService;
    @Resource
    private StdOriginalModelMapper stdOriginalModelMapper;



    @Override
    public ConvTunnel upload(String subject, Map<String, Object> paramMap) {
        if (StringUtils.isEmpty(subject)) {
            return null;
        }
        List<ConvTunnel> tunnels = convTunnelService.list(new LambdaQueryWrapper<ConvTunnel>()
                .eq(ConvTunnel::getId, Integer.valueOf(subject))
                .eq(ConvTunnel::getDelFlag, 0));
        if (CollectionUtils.isEmpty(tunnels)) {
            return null;
        }
        ConvTunnel convTunnel = tunnels.get(0);
        String collectRange = convTunnel.getCollectRange();
        final List<String> modelNames = Arrays.asList(collectRange.split(","));
        List<StdOriginalModel> modelList = stdOriginalModelMapper.selectList(
                new LambdaQueryWrapper<StdOriginalModel>()
                        .eq(StdOriginalModel::getSysCode, convTunnel.getSysCode())
                        .in(StdOriginalModel::getNameEn, modelNames)
                        .eq(StdOriginalModel::getDelFlag, 0));
        if (CollectionUtils.isEmpty(modelList)) {
            return convTunnel;
        }
        for (StdOriginalModel originalModel : modelList) {
            String tableName = originalModel.getNameEn();
            String dsId = originalModel.getConvDsConfId();
            Statement statement = null;
            try {
                statement = doCreateStatement(tableName, dsId);
                if (statement == null){
                    return convTunnel;
                }
                processDataMap(statement, paramMap, tableName);
            } catch (RuntimeException runtimeException) {
                log.error("do create statement fail! ex = {}", runtimeException.getMessage());
            }
        }
        return convTunnel;
    }

    private void processDataMap(Statement statement, Map<String, Object> paramMap, String tableName) {
        Object data = paramMap.get(tableName);
        if (data == null || statement == null) {
            return;
        }
        List<Map<String, Object>> objectList = (List<Map<String, Object>>) data;
        // 遍历对象数组，将数据插入数据库表中
        for (Map<String, Object> object : objectList) {
            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();

            for (Map.Entry<String, Object> objEntry : object.entrySet()) {
                String column = objEntry.getKey();
                Object value = objEntry.getValue();

                if (columns.length() > 0) {
                    columns.append(", ");
                    values.append(", ");
                }
                columns.append(column);
                values.append("'").append(value).append("'");
            }
            String sql = "INSERT INTO " + tableName + " (" + columns.toString() + ") VALUES (" + values.toString() + ")";
            try {
                statement.executeQuery(sql);
            } catch (SQLException sqlException) {
                continue;
            }
        }
    }

    /**
     * 创建数据库操作statement
     */
    private Statement doCreateStatement(String tableName, String dsId) {
        List<ConvOdsDatasourceConfig> dataSourceConfigs = convOdsDatasourceConfigService.list(new LambdaQueryWrapper<ConvOdsDatasourceConfig>()
                .eq(ConvOdsDatasourceConfig::getId, Integer.valueOf(dsId))
                .eq(ConvOdsDatasourceConfig::getDelFlag, 0));
        if (CollectionUtils.isEmpty(dataSourceConfigs)) {
            throw new RuntimeException(tableName + "表关联的目标数据源不存在!无法完成写入操作!dsId =" + dsId);
        }
        ConvOdsDatasourceConfig dsConf = dataSourceConfigs.get(0);
        try {
            Connection connection = DriverManager.getConnection(dsConf.getDsUrl(), dsConf.getDsUsername(), dsConf.getDsPwd());
            return connection.createStatement();
        } catch (SQLException sqlException) {
            log.error("doCreateStatement fail! ex = {}", sqlException.getMessage());
            return null;
        }
    }
}
