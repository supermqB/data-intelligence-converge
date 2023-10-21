package com.lrhealth.data.converge.scheduled.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvDataXJob;
import com.lrhealth.data.converge.scheduled.model.dto.TableInfoDto;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jinmengyu
 * @since 2023-09-14
 */
public interface ConvDataXJobService extends IService<ConvDataXJob> {

    ConvDataXJob createOrUpdateDataXJob(String jsonPath, Long tunnelId, String tableName, TableInfoDto tableInfoDto);

}
