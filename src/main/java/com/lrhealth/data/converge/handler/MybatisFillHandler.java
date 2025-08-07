package com.lrhealth.data.converge.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Date;

@Component
public class MybatisFillHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        Object originalObject = metaObject.getOriginalObject();
        Class<?> aClass = originalObject.getClass();
        Field createTime = ReflectionUtils.findField(aClass, "createTime");
        Field updateTime = ReflectionUtils.findField(aClass, "updateTime");
        Field delFlag = ReflectionUtils.findField(aClass, "delFlag");
        if (null != createTime) {
            if (Date.class == createTime.getType()) {
                this.setFieldValByName("createTime", new Date(), metaObject);
            } else {
                this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
            }
        }
        if (null != updateTime) {
            if (Date.class == updateTime.getType()) {
                this.setFieldValByName("updateTime", new Date(), metaObject);
            } else {
                this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
            }
        }
        // 处理delFlag
        if (null != delFlag) {
            if (Integer.class == delFlag.getType()) {
                this.setFieldValByName("delFlag", 0, metaObject);
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Object originalObject = metaObject.getOriginalObject();
        Class<?> aClass = originalObject.getClass();
        Field updateTime = ReflectionUtils.findField(aClass, "updateTime");
        if (null != updateTime) {
            if (updateTime.getType() == Date.class) {
                this.setFieldValByName("updateTime", new Date(), metaObject);
            } else {
                this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
            }
        }
    }

}
