package com.lrhealth.data.converge.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.dao.entity.DbIncrConfig;
import com.lrhealth.data.converge.dao.mapper.DbIncrConfigMapper;
import com.lrhealth.data.converge.dao.service.DbIncrConfigService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 数据库同步增量字段配置表 服务实现类
 * </p>
 *
 * @author jinmengyu
 * @since 2023-07-20
 */
@Service
public class DbIncrConfigServiceImpl extends ServiceImpl<DbIncrConfigMapper, DbIncrConfig> implements DbIncrConfigService {

}
