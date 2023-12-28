package com.lrhealth.data.converge.dao.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.lrhealth.data.converge.dao.entity.ConvTaskResultFile;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;

import java.io.File;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhangguowen-generator
 * @since 2023-11-06
 */
public interface ConvTaskResultFileService extends IService<ConvTaskResultFile> {

    void createTaskResultFile(ConvTunnel tunnel, Integer taskId, File file, String odsModelName);

}
