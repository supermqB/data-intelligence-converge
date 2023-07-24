package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.dao.entity.System;
import com.lrhealth.data.converge.dao.service.SystemService;
import com.lrhealth.data.converge.service.SysService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollUtil.isEmpty;

/**
 * <p>
 * 系统信息处理接口实现类
 * </p>
 *
 * @author lr
 * @since 2023/7/22
 */
@Service
public class SysServiceImpl implements SysService {
    @Resource
    private SystemService service;

    @Override
    public List<System> queryListBySysCode(List<String> sysCodeList) {
        if (isEmpty(sysCodeList)) {
            return CollUtil.newArrayList();
        }
        List<System> result = service.list(new LambdaQueryWrapper<System>().in(System::getSystemCode, sysCodeList));
        return isEmpty(result) ? CollUtil.newArrayList() : result;
    }

    @Override
    public Map<String, System> queryMapBySysCode(List<String> sysCodeList) {
        return getSysCodeMap(queryListBySysCode(sysCodeList));
    }

    @Override
    public List<System> queryListByOrgCode(List<String> orgCodeList) {
        if (isEmpty(orgCodeList)) {
            return CollUtil.newArrayList();
        }
        List<System> result = service.list(new LambdaQueryWrapper<System>().in(System::getOrgCode, orgCodeList));
        return isEmpty(result) ? CollUtil.newArrayList() : result;
    }

    @Override
    public Map<String, System> querySysMapByOrgCode(List<String> orgCodeList) {
        return getSysCodeMap(queryListByOrgCode(orgCodeList));
    }

    /**
     * 获取系统编码Map
     * Map<系统编码,系统信息>
     *
     * @param sysList 系统信息集合
     * @return 系统信息集合
     */
    public Map<String, System> getSysCodeMap(List<System> sysList) {
        if (isEmpty(sysList)) {
            return MapUtil.newHashMap();
        }
        return sysList.stream().collect(Collectors.toMap(System::getSystemCode, a -> a, (a, b) -> b));
    }


}
