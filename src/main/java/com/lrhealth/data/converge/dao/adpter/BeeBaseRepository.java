package com.lrhealth.data.converge.dao.adpter;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Repository;
import org.teasoft.bee.osql.PreparedSql;
import org.teasoft.honey.osql.core.BeeFactory;

import java.util.List;
import java.util.Map;

/**
 * 查询db
 * 直接查询数据库，有值直接返回
 */
@Slf4j
@Repository
public class BeeBaseRepository {

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

}
