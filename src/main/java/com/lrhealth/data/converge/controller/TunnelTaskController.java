package com.lrhealth.data.converge.controller;

import com.lrhealth.data.common.result.ResultBase;
import com.lrhealth.data.converge.scheduled.model.dto.TunnelMessageDTO;
import com.lrhealth.data.converge.service.ConvergeTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
    private ConvergeTaskService convergeTaskService;

    /**
     * 管道任务下发
     * @param tunnelMessageDTO
     */
    @PostMapping("/tunnel/upsert")
    public ResultBase<String> upsertTunnel(@RequestBody TunnelMessageDTO tunnelMessageDTO){
        try {
            convergeTaskService.tunnelConfig(tunnelMessageDTO);
            return ResultBase.success("管道更新成功");
        }catch (Exception e){
            log.error("upsert tunnel error: {}", ExceptionUtils.getStackTrace(e));
            return ResultBase.fail();
        }

    }

    /**
     * 任务立即执行
     * @param taskId
     * @return
     */
    @PostMapping("/tunnel/exec")
    public ResultBase<String> tunnelExec(@RequestParam("tunnelId") Long tunnelId,
                                        @RequestParam("taskId") Integer taskId){
        try {
            convergeTaskService.taskExec(taskId, tunnelId);
            return ResultBase.success("执行成功");
        }catch (Exception e){
            log.error("task exec error: {}", ExceptionUtils.getStackTrace(e));
            return ResultBase.fail();
        }

    }

}
