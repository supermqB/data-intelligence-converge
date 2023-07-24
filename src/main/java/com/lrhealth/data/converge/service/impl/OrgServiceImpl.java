package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.dao.entity.Institution;
import com.lrhealth.data.converge.dao.service.InstitutionService;
import com.lrhealth.data.converge.service.OrgService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollUtil.isEmpty;

/**
 * <p>
 * 机构处理实现类
 * </p>
 *
 * @author lr
 * @since 2023/7/22
 */
@Service
public class OrgServiceImpl implements OrgService {
    @Resource
    private InstitutionService service;

    @Override
    public List<Institution> queryOrgList(List<String> orgCodeList) {
        if (isEmpty(orgCodeList)) {
            return CollUtil.newArrayList();
        }
        List<Institution> result = service.list(new LambdaQueryWrapper<Institution>().in(Institution::getSourceCode, orgCodeList));
        return isEmpty(result) ? CollUtil.newArrayList() : result;
    }

    @Override
    public Map<String, Institution> queryOrgMap(List<String> orgCodeList) {
        return queryOrgList(orgCodeList).stream().collect(Collectors.toMap(Institution::getSourceCode, a -> a, (a, b) -> b));
    }
}
