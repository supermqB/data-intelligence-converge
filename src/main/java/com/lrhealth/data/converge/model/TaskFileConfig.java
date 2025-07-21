package com.lrhealth.data.converge.model;

import com.lrhealth.data.converge.dao.entity.*;
import lombok.AllArgsConstructor;
import lombok.Data;

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

    private ConvTaskResultFile taskResultFile;

    private String url;

    private String destPath;
}
