package com.lrhealth.data.converge.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lrhealth.data.converge.dao.entity.ConvOriginalTable;
import com.lrhealth.data.converge.model.dto.OriginalTableModelDto;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 客户端库表信息 Mapper 接口
 * </p>
 *
 * @author jinmengyu
 * @since 2024-01-03
 */
public interface ConvOriginalTableMapper extends BaseMapper<ConvOriginalTable> {

    OriginalTableModelDto getTableModelRel(@Param("oriTableName") String oriTableName,
                                           @Param("sysCode") String sysCode);

}
