package com.lrhealth.data.converge.controller;

import cn.hutool.core.text.CharSequenceUtil;
import com.lrhealth.data.converge.common.enums.TaskStatusEnum;
import com.lrhealth.data.converge.common.result.ResultBase;
import com.lrhealth.data.converge.common.util.TokenUtil;
import com.lrhealth.data.converge.dao.entity.ConvTask;
import com.lrhealth.data.converge.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.dao.service.ConvTaskService;
import com.lrhealth.data.converge.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.service.ApiTransService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 接口传输
 * @author zhuanning
 * @date 2024-07-27
 */
@Slf4j
@RestController()
@RequestMapping("/api/transmission")
public class ApiTransController {

    @Resource
    private ApiTransService apiTransService;
    @Resource
    private ConvTaskService convTaskService;
    @Resource
    private ConvTunnelService convTunnelService;


    @PostMapping("/upload")
    public ResultBase upload(HttpServletRequest request, @RequestBody Map<String,Object> paramMap){
        String token = request.getHeader("token");
        if (!TokenUtil.validateToken(token)){
            return ResultBase.fail("token校验失败");
        }
        String tunnelId = TokenUtil.parseJwtSubject(token);
        if (CharSequenceUtil.isEmpty(tunnelId)){
            return ResultBase.fail("tunnelId不存在");
        }
        ConvTunnel convTunnel = convTunnelService.getTunnelWithoutDelFlag(Long.valueOf(tunnelId));
        if (convTunnel == null){
            return ResultBase.fail("汇聚管道任务不存在!");
        }
        try {
            ConvTask convTask = convTaskService.createTask(convTunnel, false);
            boolean uploadFlag = apiTransService.upload(convTunnel, paramMap);
            convTaskService.updateTaskStatus(convTask.getId(),uploadFlag ? TaskStatusEnum.DONE : TaskStatusEnum.FAILED);
        } catch (Exception e) {
            log.info("异常信息 {}",e.getMessage());
        }
        return ResultBase.success();
    }
}
