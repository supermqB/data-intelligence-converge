package com.lrhealth.data.converge.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.Frontend;
import com.lrhealth.data.converge.dao.mapper.FrontendMapper;
import com.lrhealth.data.converge.dao.service.FrontendService;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.text.CharSequenceUtil.format;
import static cn.hutool.core.text.CharSequenceUtil.isBlank;

/**
 * <p>
 * 前置机信息 服务实现类
 * </p>
 *
 * @author jinmengyu
 * @since 2023-07-20
 */
@Service
public class FrontendServiceImpl extends ServiceImpl<FrontendMapper, Frontend> implements FrontendService {

    @Override
    public Frontend getByFrontenfCode(String frontendCode) {
        if (isBlank(frontendCode)) {
            throw new CommonException("前置机编码为空");
        }
        List<Frontend> list = this.list(new LambdaQueryWrapper<Frontend>().eq(Frontend::getFrontendCode, frontendCode));
        if (isEmpty(list)) {
            throw new CommonException(format("前置机编码={}的机器不存在", frontendCode));
        }
        if (list.size() > 1) {
            throw new CommonException(format("前置机编码={}的机器异常，存在{}条配置", frontendCode, list.size()));
        }
        return list.get(0);
    }
}
