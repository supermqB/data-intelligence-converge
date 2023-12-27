package com.lrhealth.data.converge.service;


import com.lrhealth.data.converge.model.dto.TunnelMessageDTO;

/**
 * 库表采集-直连
 * 数智 -> 汇聚 -> 汇聚同服务器前置机
 * @author jinmengyu
 * @date 2023-12-19
 */
public interface DirectConnectCollectService {

    /**
     * 下发配置
     * 调取前置机管道配置接口
     * @param dto 管道配置参数
     * @return
     */
    void tunnelConfig(TunnelMessageDTO dto);

    /**
     * 执行任务
     * 调取前置机执行任务接口
     * @param taskId
     * @param tunnelId
     */
    void tunnelExec(Integer taskId, Long tunnelId);
}
