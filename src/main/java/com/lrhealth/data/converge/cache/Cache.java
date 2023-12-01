package com.lrhealth.data.converge.cache;

import java.util.List;

/**
 * 缓存接口
 * @author zhuanning
 */
public interface Cache {

    /**
     * 存储缓存数据
     * @param key 缓存key
     * @param value 缓存键
     */
    void putObject(String key, Object value);

    /**
     * 获取缓存数据
     * @param key 缓存key
     * @return 缓存结果
     */
    Object getObject(Object key);


    /**
     * 通过前缀获取缓存key
     * @param prefix 前缀
     * @return 缓存集合
     */
    List<String> getAllByPrefix(String prefix);

    /**
     * 删除缓存数据
     * @param key 缓存key
     * @return 删除结果
     */
    Object removeObject(Object key);

    /**
     * 清除缓存
     */
    void clear();
}
