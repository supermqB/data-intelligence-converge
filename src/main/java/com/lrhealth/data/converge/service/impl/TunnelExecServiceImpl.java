package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import com.lrhealth.data.converge.common.enums.TunnelStatusEnum;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.scheduled.model.dto.TunnelMessageDTO;
import com.lrhealth.data.converge.service.AsyncExecService;
import com.lrhealth.data.converge.service.DataXExecService;
import com.lrhealth.data.converge.service.TunnelExecService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author jinmengyu
 * @date 2023-09-26
 */
@Slf4j
@Service
public class TunnelExecServiceImpl implements TunnelExecService {
    @Resource
    private DataXExecService dataXService;
    @Resource
    private AsyncExecService asyncExecService;
    @Resource
    private ConvTaskService frontendTaskService;
    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private SchedulerConfig schedulerConfig;

    @Override
    public void tunnelConfig(TunnelMessageDTO dto) {
        // 创建/更新/删除管道
        ConvTunnel tunnel = tunnelService.getById(dto.getId());
        if (ObjectUtil.isNull(tunnel)){
            schedulerConfig.cancelTriggerTask(String.valueOf(dto.getId()));
            return;
        }
        // datax执行配置
        if (TunnelCMEnum.LIBRARY_TABLE.getCode().equals(tunnel.getConvergeMethod())) {
            if (ObjectUtil.isNull(dto.getJdbcInfoDto().getTableInfoDtoList())){
                return;
            }
            dataXService.dataXConfig(tunnel, dto.getJdbcInfoDto().getTableInfoDtoList());
        }
        // 文件读取和库表采集重新配置调度
        schedulerConfig.addTask(String.valueOf(tunnel.getId()), schedulerConfig.createTriggerTask(tunnel));

    }

    @Override
    public void tunnelExec(Integer taskId, Long tunnelId) {
        ConvTunnel ft = tunnelService.getById(tunnelId);
        // 管道暂停/废弃，不指定任务
        if (ft.getStatus().equals(TunnelStatusEnum.PAUSE.getValue()) || ft.getStatus().equals(TunnelStatusEnum.ABANDON.getValue())) {
            return;
        }
        boolean isCdc = TunnelCMEnum.CDC_LOG.getCode().equals(ft.getConvergeMethod());
        if (TunnelStatusEnum.PROCESSING.getValue().equals(ft.getStatus()) && isCdc) {
            return;
        }

        // 更新tunnel状态为任务执行中
        tunnelService.updateById(ConvTunnel.builder().id(tunnelId).status(TunnelStatusEnum.PROCESSING.getValue()).build());

        asyncExecService.taskExec(ft, taskId);
    }

}
