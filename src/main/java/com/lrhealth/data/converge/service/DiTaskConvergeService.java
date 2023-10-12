package com.lrhealth.data.converge.service;

/**
 * @author jinmengyu
 * @date 2023-10-12
 */
public interface DiTaskConvergeService {

    /**
     * 数据汇聚流程
     * 文件处在汇聚服务器
     * 通过di_conv_task_result_view新建xds,数据落库后再进行更新
     * 异步执行
     */
    void fileParseAndSave();
}
