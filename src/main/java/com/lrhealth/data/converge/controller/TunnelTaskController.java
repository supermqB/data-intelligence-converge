package com.lrhealth.data.converge.controller;

import com.lrhealth.data.common.result.ResultBase;
import com.lrhealth.data.converge.scheduled.model.dto.TunnelMessageDTO;
import com.lrhealth.data.converge.scheduled.service.convergeTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author jinmengyu
 * @date 2023-09-18
 */
@Slf4j
@RestController
@RequestMapping("/task")
public class TunnelTaskController {
    @Resource
    private convergeTaskService convergeTaskService;

    /**
     * 管道任务下发
     * @param tunnelMessageDTO
     */
    @PostMapping("/tunnel/upsert")
    public ResultBase<Void> upsertTunnel(@RequestBody TunnelMessageDTO tunnelMessageDTO){
        convergeTaskService.tunnelConfig(tunnelMessageDTO);
        return ResultBase.success();
    }

    /**
     * 任务立即执行
     * @param taskId
     * @return
     */
    @PostMapping("/tunnel/exec")
    public ResultBase<Void> tunnelExec(@RequestParam("tunnelId") Long tunnelId,
                                        @RequestParam("taskId") Integer taskId){
        convergeTaskService.taskExec(taskId, tunnelId);
        return ResultBase.success();
    }

}
