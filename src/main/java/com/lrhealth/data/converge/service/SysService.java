package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.System;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 系统处理
 * </p>
 *
 * @author lr
 * @since 2023/7/22
 */
public interface SysService {
    /**
     * 根据系统编码查询系统信息
     *
     * @param sysCodeList 系统编码集合
     * @return 系统信息
     */
    List<System> queryListBySysCode(List<String> sysCodeList);

    /**
     * 根据系统编码查询系统信息-Map
     * Map<系统编码,系统信息>
     *
     * @param sysCodeList 系统编码集合
     * @return 系统信息
     */
    Map<String, System> queryMapBySysCode(List<String> sysCodeList);

    /**
     * 根据机构编码查询系统信息
     *
     * @param orgCodeList 机构编码集合
     * @return 系统信息
     */
    List<System> queryListByOrgCode(List<String> orgCodeList);

    /**
     * 根据机构编码查询系统信息-Map
     * Map<系统编码,系统信息>
     *
     * @param orgCodeList 机构编码集合
     * @return 系统信息
     */
    Map<String, System> querySysMapByOrgCode(List<String> orgCodeList);
}
