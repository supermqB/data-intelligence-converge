package com.lrhealth.data.converge.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.dao.mapper.XdsMapper;
import com.lrhealth.data.converge.dao.service.XdsService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * XDS信息接口实现类
 * </p>
 *
 * @author lr
 * @since 2023/7/19
 */
@Service
public class XdsServiceImpl extends ServiceImpl<XdsMapper, Xds> implements XdsService {
}
