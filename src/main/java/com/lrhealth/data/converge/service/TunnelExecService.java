package com.lrhealth.data.converge.service;


import com.lrhealth.data.converge.model.dto.TunnelMessageDTO;

/**
 * @author jinmengyu
 * @date 2023-09-26
 */
public interface TunnelExecService {

    /**
     * 调度生成任务
     * @param taskId
     * @param tunnelId
     */
    void tunnelExec(Integer taskId, Long tunnelId);

    /**
     *  管道配置的创建和更新
     * @param dto 管道配置参数
     * @return
     */
    void tunnelConfig(TunnelMessageDTO dto);

}
