package com.lrhealth.data.converge.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 *
 * @author hui
 * @date 2022-05-24-18:32
 */
@Component
public class RedisClient {
    private static final Logger log = LoggerFactory.getLogger(RedisClient.class);
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 字符串类型数据保存
     *
     * @param key   键
     * @param value 值
     * @param time  缓冲时间
     */
    public synchronized void set(String key, String value, long time) {
        if (time > 0) {
            redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    /**
     * 字符串类型数据保存
     *
     * @param key   键
     * @param value 值
     */
    public synchronized void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 读取缓存
     *
     * @param key
     *
     * @return
     */
    public Object get(final String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除指定key
     *
     * @param key 要删除的key
     */
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 批量删除指定前缀的key
     *
     * @param prexKey prexKey
     */
    public void deletePrexKeys(String prexKey) {
        Set<String> keys = redisTemplate.keys(prexKey + "*");
        if (CollectionUtil.isNotEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 模糊查询key
     *
     * @param prexKey prexKey
     */
    public synchronized Set<String> getKeys(String prexKey) {
        try {
            Set<String> result = redisTemplate.keys("*" + prexKey + "*");
            if (CollUtil.isNotEmpty(result)){
                return result;
            }
        }catch (Exception e){
            log.error("判断key是否存在:", e);
        }
        return null;
    }

    /**
     * 判断key是否存在
     *
     * @param key Key
     *
     * @return 是否存在
     */
    public synchronized boolean exists(final String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("判断key是否存在:", e);
        }
        return false;
    }

    /**
     * list写入redis
     *
     * @param key       键
     * @param valueList list集合
     * @param time      过期时间 负数永不过期
     */
    public synchronized void saveList(String key, List valueList, long time) {
        if (time > 0) {
            redisTemplate.opsForList().rightPushAll(key, valueList, time, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForList().rightPushAll(key, valueList);
        }
    }

    /**
     * list写入redis
     *
     * @param key 键
     */
    public synchronized void saveSet(String key, String value) {
        redisTemplate.opsForSet().add(key, value);
    }


    /**
     * 查询指定key的所有list元素
     *
     * @param key 键
     *
     * @return List<Object>
     */
    public List<Object> queryList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 删除key对应list中的指定元素
     *
     * @param key   键
     * @param value 要删除的value
     */
    public void delFromList(String key, Object value) {
        redisTemplate.opsForList().remove(key, 0, value);
    }

    /**
     * key的value 为数字类型  自增
     *
     * @param key
     * @param delta
     *
     * @return
     */
    @Async
    public void incr(String key, long delta) {
        redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 设置数字值
     *
     * @param key
     * @param value
     * @param time
     */
    public void setNum(String key, Long value, long time) {
        if (time > 0) {
            redisTemplate.opsForValue().set(key, value.toString(), time, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }
}
