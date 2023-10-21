package com.lrhealth.data.converge.scheduled.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.Field;

/**
 * @author jinmengyu
 * @date 2023-09-14
 */
@Slf4j
public class SchedulerUtil {

    public static Object getProperty(Object obj, String name) throws NoSuchFieldException {
        Object value = null;
        Field field = findField(obj.getClass(), name);
        if (field == null) {
            throw new NoSuchFieldException("no such field [" + name + "]");
        }
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        try {
            value = field.get(obj);
        } catch (Exception e) {
            log.error("log error,{}", ExceptionUtils.getStackTrace(e));
        }
        field.setAccessible(accessible);
        return value;
    }

    public static Field findField(Class<?> clazz, String name) {
        try {
            return clazz.getField(name);
        } catch (NoSuchFieldException ex) {
            return findDeclaredField(clazz, name);
        }
    }

    public static Field findDeclaredField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException ex) {
            if (clazz.getSuperclass() != null) {
                return findDeclaredField(clazz.getSuperclass(), name);
            }
            return null;
        }
    }
}
