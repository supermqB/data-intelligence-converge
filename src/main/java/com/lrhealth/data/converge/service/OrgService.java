package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.Institution;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 机构处理
 * </p>
 *
 * @author lr
 * @since 2023/7/22
 */
public interface OrgService {
    /**
     * 根据机构编码查询机构信息
     *
     * @param orgCodeList 机构编码集合
     * @return 机构信息
     */
    List<Institution> queryOrgList(List<String> orgCodeList);

    /**
     * 根据机构编码查询机构信息-Map
     * Map<机构编码,机构信息>
     *
     * @param orgCodeList 机构编码集合
     * @return 机构信息
     */
    Map<String, Institution> queryOrgMap(List<String> orgCodeList);
}
