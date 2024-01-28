package com.lrhealth.data.converge.common.config.db;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lrhealth.data.converge.dao.entity.ConvOdsDatasourceConfig;
import com.lrhealth.data.converge.dao.service.ConvOdsDatasourceConfigService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.teasoft.honey.osql.core.BeeFactory;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * ods数据源仓库工具
 */
@Slf4j
@Repository
public class DataSourceRepository {
    // 默认数据源的连接池配置
    private static final String DEFAULT_DS_POOL_CONFIG = "{\n" +
            "    \"minIdle\":5,\n" +
            "    \"idleTimeout\":120000,\n" +
            "    \"maxPoolSize\":10,\n" +
            "    \"isAutoCommit\":true,\n" +
            "    \"maxLifetime\":1800000,\n" +
            "    \"connectionTimeout\":30000,\n" +
            "    \"connectionTestQuery\":\"SELECT 1\"\n" +
            "}";

    @Resource
    private ConvOdsDatasourceConfigService convOdsDatasourceConfigService;


    /**
     * 初始化所有的ods数据源
     */
    public void initOdsDataSource() {
        QueryWrapper<ConvOdsDatasourceConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);
        queryWrapper.eq("ds_type", 1);
        List<ConvOdsDatasourceConfig> list = convOdsDatasourceConfigService.list(queryWrapper);
        list.forEach(ds -> createDataSource(ds));
    }

    /**
     * 创建单个数据源
     * @param ds
     */
    public void createDataSource(ConvOdsDatasourceConfig ds) {
        Map<String, DataSource> dataSourceMap = BeeFactory.getInstance().getDataSourceMap();
        if(dataSourceMap.get(ds.getSysCode()) != null){
            return;
        }
        try {
            dataSourceMap.put(ds.getSysCode(), new HikariDataSource(buildHikariConfig(ds)));
            // 将新的数据源加入到bee框架，该数据源即时生效
            BeeFactory.getInstance().setDataSourceMap(dataSourceMap);
        }catch (Exception e){
            log.error("datasource set error");
        }

    }

    /**
     * 删除单个数据源
     * @param dataSourceName
     */
    public void removeDataSource(String dataSourceName) {
        Map<String, DataSource> dataSourceMap = BeeFactory.getInstance().getDataSourceMap();
        if(dataSourceMap.get(dataSourceName) == null){
            return;
        }
        dataSourceMap.remove(dataSourceName);
        // 将新的数据源从bee框架删除，该数据源即时失效
        BeeFactory.getInstance().setDataSourceMap(dataSourceMap);
    }

    /**
     * 构建Hikari数据源配置
     * @param ds
     * @return
     */
    private HikariConfig buildHikariConfig(ConvOdsDatasourceConfig ds) {
        HikariConfig config = new HikariConfig();
        JSONObject jsonObject = JSONObject.parseObject(DEFAULT_DS_POOL_CONFIG);
        if(!StringUtils.isEmpty(ds.getDsPoolConfig())){
            jsonObject = JSONObject.parseObject(ds.getDsPoolConfig());
        }
        Field[] fields = HikariConfig.class.getDeclaredFields();

        // 基础配置
        config.setDriverClassName(ds.getDsDriverName());
        config.setJdbcUrl(ds.getDsUrl());
        config.setUsername(ds.getDsUsername());
        config.setPassword(ds.getDsPwd());
        config.setPoolName("HikariCP-"+ ds.getSysCode());

        /**
         * 反射设置HikariConfig连接池配置.
         * 注意：ds.getDsPoolConfig()存放的JSONObject，key名称需要与HikariConfig的属性名称一致。
         */
        reflexSettingsPoolConfig(config, jsonObject, fields);
        return config;
    }

    private void reflexSettingsPoolConfig(HikariConfig config, JSONObject jsonObject, Field[] fields) {
        try{
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                for (Field field : fields) {
                    if(entry.getKey().equals(field.getName())){
                        // 获取字段的类型
                        Class<?> fieldType = field.getType();
                        // 将 JSON 值转换为对应的属性类型，并设置到 HikariConfig 对象中
                        setFieldValue(config, field, entry.getValue(), fieldType);
                        break;
                    }
                }
            }
        }catch (Exception e){
            log.error("DataSourceRepository，反射设置HikariConfig连接池配置异常",e);
        }
    }

    private static void setFieldValue(HikariConfig config, Field field, Object jsonValue, Class<?> fieldType) throws IllegalAccessException{
        field.setAccessible(true);
        if (fieldType == int.class || fieldType == Integer.class) {
            field.setInt(config, ((Number) jsonValue).intValue());
        } else if (fieldType == long.class || fieldType == Long.class) {
            field.setLong(config, ((Number) jsonValue).longValue());
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            field.setBoolean(config, (boolean) jsonValue);
        } else if (fieldType == String.class) {
            field.set(config, jsonValue.toString());
        }
    }
}