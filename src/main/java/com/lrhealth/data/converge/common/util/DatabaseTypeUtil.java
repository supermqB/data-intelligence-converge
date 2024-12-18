package com.lrhealth.data.converge.common.util;

import cn.hutool.core.text.CharSequenceUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
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

    private static final Logger log = LoggerFactory.getLogger(DatabaseTypeUtil.class);

    private static final Map<String, String> javaFieldTypeMap = new HashMap<>();

    static {
        Field[] declaredFields = Types.class.getDeclaredFields();
        for (Field field : declaredFields){
            try {
                javaFieldTypeMap.put(field.get(Types.class).toString(), field.getName());
            } catch (IllegalAccessException e) {
                log.error("启动加载java数据类型失败， {}", ExceptionUtils.getStackTrace(e));
            }
        }
    }

    public static String getJavaType(String dataType){
        if (CharSequenceUtil.isBlank(dataType)) return null;
        if (javaFieldTypeMap.containsKey(dataType)){
            return javaFieldTypeMap.get(dataType);
        }
        return null;
    }

}
