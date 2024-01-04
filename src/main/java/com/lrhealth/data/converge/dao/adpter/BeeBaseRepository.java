package com.lrhealth.data.converge.dao.adpter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.lrhealth.data.common.enums.db.DataSourceEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.teasoft.bee.osql.*;
import org.teasoft.honey.distribution.GenIdFactory;
import org.teasoft.honey.osql.core.BeeFactory;
import org.teasoft.honey.osql.core.BeeFactoryHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 查询db
 * 直接查询数据库，有值直接返回
 */
@Slf4j
@Repository
public class BeeBaseRepository {

    public Map<String, Object> selectOne(String sysCode,String tableName, Map<String, Object> conditionMap) {
        return selectOne(sysCode,tableName, null, conditionMap);
    }

    public Map<String, Object> selectOne(String sysCode,String tableName, String selectColumns, Map<String, Object> conditionMap) {
        MapSuid mapSuid = BeeFactoryHelper.getMapSuid();
        mapSuid.setDataSourceName(sysCode);
        MapSql mapSql = BeeFactoryHelper.getMapSql();

        mapSql.put(MapSqlKey.Table, tableName);
        if (StringUtils.isEmpty(selectColumns) || StrUtil.equalsAnyIgnoreCase(selectColumns, "*")) {
            mapSql.put(MapSqlKey.SelectColumns, "*");
        }
        mapSql.put(MapSqlSetting.IsNamingTransfer, true);
        mapSql.put(MapSqlSetting.IsIncludeNull, true);

        for (Map.Entry<String, Object> stringObjectEntry : conditionMap.entrySet()) {
            mapSql.put(((Map.Entry<?, ?>) stringObjectEntry).getKey().toString(), stringObjectEntry.getValue());
        }
        Map<String, Object> selectOne = mapSuid.selectOne(mapSql);
        return selectOne;
    }

    public List<Map<String, Object>> selectList(String sysCode,String tableName, Map<String, Object> conditionMap) {
        return selectList(sysCode,tableName, null, conditionMap, null);
    }

    public List<Map<String, Object>> selectList(String sysCode,String tableName, Map<String, Object> conditionMap, String orderBy) {
        return selectList(sysCode,tableName, null, conditionMap, orderBy);
    }

    public List<Map<String, Object>> selectList(String sysCode,String tableName, Map<String, Object> conditionMap, Integer start, Integer size) {
        return selectList(sysCode,tableName, null, conditionMap, start, size);
    }

    public List<Map<String, Object>> selectList(String sysCode,String tableName, Map<String, Object> conditionMap, String orderBy, Integer start, Integer size) {
        return selectList(sysCode,tableName, null, conditionMap, orderBy, start, size);
    }

    public List<Map<String, Object>> selectList(String sysCode,String tableName, String selectColumns, Map<String, Object> conditionMap, String orderBy) {
        return selectList(sysCode,tableName, selectColumns, conditionMap, orderBy, null, null);
    }

    public List<Map<String, Object>> selectList(String sysCode,String tableName, String selectColumns, Map<String, Object> conditionMap, Integer start, Integer size) {
        return selectList(sysCode,tableName, selectColumns, conditionMap, "", start, size);
    }

    public List<Map<String, Object>> selectList(String sysCode,String tableName, String selectColumns, Map<String, Object> conditionMap, String orderBy, Integer start, Integer size) {
        MapSuid mapSuid = BeeFactoryHelper.getMapSuid();
        mapSuid.setDataSourceName(sysCode);
        MapSql mapSql = BeeFactoryHelper.getMapSql();

        mapSql.put(MapSqlKey.Table, tableName);
        if (StringUtils.isEmpty(selectColumns) || StrUtil.equalsAnyIgnoreCase(selectColumns, "*")) {
            mapSql.put(MapSqlKey.SelectColumns, "*");
        }else {
            mapSql.put(MapSqlKey.SelectColumns, selectColumns);
        }
        if (StringUtils.isNotEmpty(orderBy)) {
            mapSql.put(MapSqlKey.OrderBy, orderBy);
        }
        mapSql.put(MapSqlSetting.IsNamingTransfer, true);
        mapSql.put(MapSqlSetting.IsIncludeNull, true);
        mapSql.start(start);
        mapSql.size(size);
        if(CollectionUtils.isEmpty(conditionMap)){
            log.warn("query {} without where condition",tableName);
        }else{
            for (Map.Entry<String, Object> stringObjectEntry : conditionMap.entrySet()) {
                mapSql.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
            }
        }

        List<Map<String, Object>> mapList = Lists.newArrayList();
        try {
            mapList = mapSuid.select(mapSql);
        } catch (Exception e) {
            log.error("preparedSql selectList error. table:{},condition:{},{}", tableName, JSON.toJSONString(conditionMap), ExceptionUtils.getStackTrace(e));
        }
        return mapList;
    }

    public <T> List<T> selectList(String sysCode,T entity) {
        Suid suid = BeeFactoryHelper.getSuid();
        suid.setDataSourceName(DataSourceEnum.ODS.getValue());
        return suid.select(entity);
    }

    public List<Map<String, Object>> selectListBySql(String sysCode,String sql) {
        if (StringUtils.isEmpty(sql)) {
            return null;
        }
        PreparedSql preparedSql = BeeFactory.getHoneyFactory().getPreparedSql();
        preparedSql.setDataSourceName(sysCode);
        return preparedSql.selectMapList(sql);
    }

    public int selectCount(String sysCode,String tableName, Map<String, Object> conditionMap) {
        MapSuid mapSuid = BeeFactoryHelper.getMapSuid();
        mapSuid.setDataSourceName(sysCode);
        MapSql mapSql = BeeFactoryHelper.getMapSql();

        mapSql.put(MapSqlKey.Table, tableName);

        for (Map.Entry<String, Object> stringObjectEntry : conditionMap.entrySet()) {
            mapSql.put(((Map.Entry<?, ?>) stringObjectEntry).getKey().toString(), stringObjectEntry.getValue());
        }
        return mapSuid.count(mapSql);
    }

    public long insert(String sysCode,String tableName, Map<String, Object> insertValueMap) {
        MapSuid mapSuid = BeeFactoryHelper.getMapSuid();
        mapSuid.setDataSourceName(sysCode);
        MapSql insertMapSql = BeeFactoryHelper.getMapSql();
        insertMapSql.put(MapSqlKey.Table, tableName);
        insertMapSql.put(MapSqlSetting.IsNamingTransfer, true);
        for (Map.Entry<String, Object> stringObjectEntry : insertValueMap.entrySet()) {
            insertMapSql.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
        }
        long id = GenIdFactory.get(tableName);
        insertMapSql.put("id", id); // 分布式ID

        try {
            mapSuid.insert(insertMapSql);
        } catch (Exception e) {
            log.error("preparedSql insert error. table:{}, exception:{}", tableName, ExceptionUtils.getStackTrace(e));
        }
        return id;
    }

    public int update(String sysCode,String tableName, Map<String, Object> setValueMap, Map<String, Object> whereValueMap) {
        MapSuid mapSuid = BeeFactoryHelper.getMapSuid();
        mapSuid.setDataSourceName(sysCode);
        MapSql updateMapSql = BeeFactoryHelper.getMapSql();

        updateMapSql.put(MapSqlKey.Table, tableName);
        updateMapSql.put(MapSqlSetting.IsNamingTransfer, false);
        updateMapSql.put(MapSqlSetting.IsIncludeEmptyString, false);

        for (Map.Entry<String, Object> stringObjectEntry : setValueMap.entrySet()) {
            updateMapSql.putNew(stringObjectEntry.getKey(), stringObjectEntry.getValue());
        }
        for (Map.Entry<String, Object> stringObjectEntry : whereValueMap.entrySet()) {
            updateMapSql.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
        }
        return mapSuid.update(updateMapSql);
    }

    /**
     * 批量插入\更新，使用 INSERT INTO ... VALUES ...
     *
     * @param tableName           数据库表
     * @param insertActionMapList 待插入的表数据
     */
    public void insertBatch(String sysCode,String tableName, List<Map<String, Object>> insertActionMapList) {
        if (CollUtil.isEmpty(insertActionMapList)) {
            return;
        }
        StringBuilder fieldSql = new StringBuilder();
        StringBuilder valueSql = new StringBuilder();
        Map<String, Object> parameterMap = insertActionMapList.get(0);
        for (String str : parameterMap.keySet()) {
            fieldSql.append(str).append(",");
            valueSql.append("#{").append(str).append("},");
        }
        String sql = "INSERT INTO " + tableName + "(" +
                fieldSql.substring(0, fieldSql.toString().length() - 1) + ")" +
                " VALUES " + "(" +
                valueSql.substring(0, valueSql.toString().length() - 1) + ")";
        PreparedSql preparedSql = BeeFactory.getHoneyFactory().getPreparedSql();
        preparedSql.setDataSourceName(sysCode);
        try {
            preparedSql.insertBatch(sql, insertActionMapList);
        } catch (Exception e) {
            log.error("preparedSql insertBatch error. table:{}, exception:{}", tableName, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * 批量插入\更新，使用 INSERT INTO ... VALUES ... ON DUPLICATE ...
     *
     * @param tableName           数据库表
     * @param upsertActionMapList 待插入\更新的表数据
     */
//    public void upsertBatch(String sysCode,String tableName, List<Map<String, Object>> upsertActionMapList) {
//        if (CollUtil.isEmpty(upsertActionMapList)) {
//            return;
//        }
//        PreparedSql preparedSql = BeeFactory.getHoneyFactory().getPreparedSql();
//        preparedSql.setDataSourceName(sysCode);
//        String sql;
//        Set<String> parameters = upsertActionMapList.get(0).keySet();
//        if (HoneyUtil.isMysql()) {
//            sql = buildSql4UpsertBatch2Mysql(tableName, parameters);
//        } else {
//            sql = buildSql4UpsertBatch2Pg(tableName, parameters);
//        }
//        try {
//            preparedSql.insertBatch(sql, upsertActionMapList);
//        } catch (Exception e) {
//            log.error("preparedSql upsertBatch error. table:{}, exception: {}", tableName, ExceptionUtils.getStackTrace(e));
//        }
//    }

    /**
     * 构造批量插入MySQL数据库的 upsert-sql
     *
     * @param tableName  数据库表
     * @param parameters 待插入的表字段
     *
     * @return upsert-sql
     */
//    private String buildSql4UpsertBatch2Mysql(String tableName, Set<String> parameters) {
//        StringBuilder fieldSql = new StringBuilder();
//        StringBuilder valueSql = new StringBuilder();
//        StringBuilder setSql = new StringBuilder();
//        for (String str : parameters) {
//            fieldSql.append(str).append(",");
//            valueSql.append("#{").append(str).append("},");
//            if (!CommonConstant.TECH_FIELD_EVENT_ID.equals(str) && !"id".equals(str)) {
//                setSql.append(str).append("=").append("VALUES(").append(str).append("),");
//            }
//        }
//        return "INSERT INTO " + tableName + "(" +
//                fieldSql.substring(0, fieldSql.toString().length() - 1) + ")" +
//                " VALUES " + "(" +
//                valueSql.substring(0, valueSql.toString().length() - 1) +
//                ") ON DUPLICATE KEY UPDATE " +
//                setSql.substring(0, setSql.toString().length() - 1);
//    }

    /**
     * 构造批量插入pg数据库的 upsert-sql
     *
     * @param tableName  数据库表
     * @param parameters 待插入的表字段
     *
     * @return upsert-sql
     */
//    private String buildSql4UpsertBatch2Pg(String tableName, Set<String> parameters) {
//        StringBuilder fieldSql = new StringBuilder();
//        StringBuilder valueSql = new StringBuilder();
//        StringBuilder setSql = new StringBuilder();
//        for (String str : parameters) {
//            fieldSql.append(str).append(",");
//            valueSql.append("#{").append(str).append("},");
//            if (!CommonConstant.TECH_FIELD_EVENT_ID.equals(str)) {
//                setSql.append(str).append("=").append("excluded.").append(str).append(",");
//            }
//        }
//        return "INSERT INTO " + tableName + "(" +
//                fieldSql.substring(0, fieldSql.toString().length() - 1) + ")" +
//                " VALUES " + "(" +
//                valueSql.substring(0, valueSql.toString().length() - 1) +
//                ")  ON CONFLICT (" + CommonConstant.TECH_FIELD_EVENT_ID + ") DO UPDATE SET " +
//                setSql.substring(0, setSql.toString().length() - 1);
//    }

    /**
     * 批量插入\更新，使用 INSERT INTO ... VALUES ... ON CONFLICT (id) DO UPDATE SET ...
     *
     * @param tableName           数据库表
     * @param upsertActionMapList 待插入\更新的表数据
     */
    public void upsertBatchPgById(String sysCode,String tableName, List<Map<String, Object>> upsertActionMapList) {
        if (CollUtil.isEmpty(upsertActionMapList)) {
            return;
        }
        PreparedSql preparedSql = BeeFactory.getHoneyFactory().getPreparedSql();
        preparedSql.setDataSourceName(sysCode);
        Set<String> parameters = upsertActionMapList.get(0).keySet();
        StringBuilder fieldSql = new StringBuilder();
        StringBuilder valueSql = new StringBuilder();
        StringBuilder setSql = new StringBuilder();
        for (String str : parameters) {
            fieldSql.append(str).append(",");
            valueSql.append("#{").append(str).append("},");
            if (!"id".equals(str)) {
                setSql.append(str).append("=").append("excluded.").append(str).append(",");
            }
        }
        String sql = "INSERT INTO " + tableName + "(" +
                fieldSql.substring(0, fieldSql.toString().length() - 1) + ")" +
                " VALUES " + "(" +
                valueSql.substring(0, valueSql.toString().length() - 1) +
                ")  ON CONFLICT (" + "id" + ") DO UPDATE SET " +
                setSql.substring(0, setSql.toString().length() - 1);

        preparedSql.insertBatch(sql, upsertActionMapList);
    }
}
