package com.lrhealth.data.converge.cache.impl;

import com.lrhealth.data.converge.cache.Cache;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 汇聚redis缓存
 * @author zhuanning
 */
@Component
public class ConvRedisCache implements Cache {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void putObject(String key, Object value) {
        redisTemplate.opsForValue().set(key,value);
    }

    @Override
    public Object getObject(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public List<String> getAllByPrefix(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix+"*");
        if (CollectionUtils.isEmpty(keys)){
            return Collections.emptyList();
        }
        return new ArrayList<>(keys);
    }

    @Override
    public Object removeObject(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public void clear() {

    }

}
