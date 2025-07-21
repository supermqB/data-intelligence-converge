package com.lrhealth.data.converge.dao.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.lrhealth.data.converge.dao.entity.ConvTaskResultCdc;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhangguowen-generator
 * @since 2023-09-18
 */
public interface ConvTaskResultCdcService extends IService<ConvTaskResultCdc> {

    void insertOrUpdateTaskResultCdc(ConvTaskResultCdc taskResultCdc);
}
