package com.lrhealth.data.converge.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lrhealth.data.converge.dao.entity.Frontend;

/**
 * <p>
 * 前置机信息 服务类
 * </p>
 *
 * @author jinmengyu
 * @since 2023-07-20
 */
public interface FrontendService extends IService<Frontend> {

    Frontend getByFrontendCode(String frontendCode);

}
