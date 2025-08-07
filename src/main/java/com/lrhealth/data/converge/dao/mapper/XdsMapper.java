package com.lrhealth.data.converge.dao.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lrhealth.data.converge.dao.entity.Xds;

/**
 * <p>
 * 汇聚Xds信息;(conv_xds)表数据库访问层
 * </p>
 *
 * @author lr
 * @since 2023/7/19
 */
@Mapper
public interface XdsMapper extends BaseMapper<Xds> {
    /**
     * 分页查询指定行数据
     *
     * @param page    分页参数
     * @param wrapper 动态查询条件
     * @return 分页对象列表
     */
    // IPage<Xds> selectByPage(IPage<Xds> page, @Param(Constants.WRAPPER)
    // Wrapper<Xds> wrapper);
}