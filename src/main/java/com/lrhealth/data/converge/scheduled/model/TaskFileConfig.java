package com.lrhealth.data.converge.scheduled.model;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvFeNode;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTask;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultView;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

/**
 * @author zhaohui
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class TaskFileConfig {

    private ConvTask convTask;

    private FileTask frontNodeTask;

    private ConvTunnel tunnel;

    private ConvFeNode feNode;

    private ConvTaskResultView taskResultView;

    private String url;

    private String destPath;
}
