package com.lrhealth.data.converge.dao.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.lrhealth.data.converge.dao.entity.StdOriginalModel;
import com.lrhealth.data.converge.dao.entity.StdOriginalModelColumn;

import java.util.List;

/**
 * <p>
 * 原始模型表信息 服务类
 * </p>
 *
 * @author lr
 * @since 2023-08-09
 */

public interface StdOriginalModelService extends IService<StdOriginalModel> {

    List<StdOriginalModelColumn> queryModelAndColumnByCatalogId();

}
