package com.lrhealth.data.converge.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lrhealth.data.converge.dao.entity.StdOriginalModel;
import com.lrhealth.data.converge.dao.entity.StdOriginalModelColumn;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author admin
 */
@Mapper
public interface StdOriginalModelMapper extends BaseMapper<StdOriginalModel> {

    List<StdOriginalModelColumn> queryModelAndColumnByCatalogId();


    List<StdOriginalModel> queryBusinessModelIdAndBMId();
}
