package com.lrhealth.data.converge.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.dao.entity.TaskInfo;
import com.lrhealth.data.converge.dao.mapper.TaskInfoMapper;
import com.lrhealth.data.converge.dao.service.TaskInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 汇聚任务信息 服务实现类
 * </p>
 *
 * @author jinmengyu
 * @since 2023-07-20
 */
@Service
public class TaskInfoServiceImpl extends ServiceImpl<TaskInfoMapper, TaskInfo> implements TaskInfoService {

}
