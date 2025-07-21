package com.lrhealth.data.converge.common.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jinmengyu
 * @date 2024-12-17
 */
public class DatabaseTypeUtil {

    private DatabaseTypeUtil(){}

    private static final Logger log = LoggerFactory.getLogger(DatabaseTypeUtil.class);

    private static final Map<Integer, String> TYPE_NAME_MAP = new HashMap<>();

    private static final Map<String,Integer> NAME_TYPE_MAP = new HashMap<>();


    static {
        Field[] declaredFields = Types.class.getDeclaredFields();
        for (Field field : declaredFields){
            try {
                TYPE_NAME_MAP.put((Integer) field.get(Types.class), field.getName());
                NAME_TYPE_MAP.put(field.getName(), (Integer) field.get(Types.class));
            } catch (IllegalAccessException e) {
                log.error("启动加载java数据类型失败， {}", ExceptionUtils.getStackTrace(e));
            }
        }
    }

    public static String getJavaType(Integer dataType){
        if (dataType == null) return null;
        if (TYPE_NAME_MAP.containsKey(dataType)){
            return TYPE_NAME_MAP.get(dataType);
        }
        return null;
    }

    public static Integer getJavaTypeName(String sqlType){
        double similarity = 0;
        Integer similarType = null;
        for (Map.Entry<String,Integer> javaType : NAME_TYPE_MAP.entrySet()){
            Integer dataType = javaType.getValue();
            String typeName = javaType.getKey();
            if (sqlType.equalsIgnoreCase(typeName)){
                return dataType;
            }
            int distance = LevenshteinDistance.getDefaultInstance().apply(sqlType, typeName);
            double typeSimilarity = 1 - (double) distance / Math.max(sqlType.length(), typeName.length());
            if (similarity < typeSimilarity) similarType = dataType;
        }
        return similarType;
    }

}
