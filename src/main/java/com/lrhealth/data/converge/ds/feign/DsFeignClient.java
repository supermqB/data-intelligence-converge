package com.lrhealth.data.converge.ds.feign;

import cn.hutool.core.util.IdUtil;
import com.lrhealth.data.converge.ds.dto.DsResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ds调用类
 */
@Component
public class DsFeignClient {
    @Resource
    private DsFeign dsFeign;

    @Value("${ds.token}")
    private String token;

    /**
     * 创建项目
     * @param projectName 项目名称
     * @return 返回项目的code,isSuccess()为true代表成功,样例如下：
     *  {
     * 	"code": 0,
     * 	"msg": "成功",
     * 	"data": {
     * 		"id": 4,
     * 		"code": 12270962191168
     *   }
     * }
     */
    public DsResult createProject(String projectName){
        DsResult dsResult = dsFeign.createProject(token, projectName, "");
        return dsResult;
    }

    /**
     * 创建工作流
     * @param projectCode 项目编码
     * @param flowName 工作流名称
     * @return 返回工作流的code,isSuccess()为true代表成功,样例如下：
     *  {
     * 	"code": 0,
     * 	"msg": "成功",
     * 	"data": {
     * 		"id": null,
     * 		"code": 12282482671680
     *   }
     * }
     */
    public DsResult createFlow(long projectCode,String flowName){
        long taskId = IdUtil.getSnowflakeNextId();
        String locations="[{\"taskCode\":"+taskId+",\"x\":279,\"y\":41}]";
        String taskRelationJson="[{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":"+
                taskId+",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}}]";
        String taskDefinitionJson="[{\"code\":"+taskId+",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"isCache\":\"NO\",\"name\":\"shell-test\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"test\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\",\"cpuQuota\":-1,\"memoryMax\":-1,\"taskExecuteType\":\"BATCH\"}]";
        DsResult dsResult = dsFeign.createProcessDefinition(token, projectCode, flowName,"",locations,
                taskRelationJson,taskDefinitionJson);
        return dsResult;
    }

    /**
     * 上线工作流
     * @param projectCode 项目编码
     * @param flowCode 工作流编码
     * @param releaseState 上线标识
     * @return {"code":0,"failed":false,"msg":"success","success":true}
     */
    public DsResult releaseFlow(long projectCode, long flowCode, String releaseState){
        DsResult dsResult = dsFeign.releaseProcessDefinition(token,projectCode, flowCode, releaseState);
        return dsResult;
    }

    /**
     * 运行工作流
     * @param projectCode 项目编码
     * @param flowCode 工作流编码
     * @return {"code":0,"data":"12283572255392","failed":false,"msg":"success","success":true}
     */
    public  DsResult startFlow(long projectCode, long flowCode){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String nowDate = sdf.format(new Date());
        nowDate = nowDate + " 00:00:00";
        String scheduleTime = "{\"complementStartDate\":\""+nowDate+"\",\"complementEndDate\":\""+nowDate+"\"}";

        DsResult dsResult = dsFeign.startProcessInstance(token, projectCode, flowCode, scheduleTime, "END",
                "TASK_POST", "START_PROCESS", "NONE", "RUN_MODE_SERIAL",
                "MEDIUM", "OFF_MODE", "DESC_ORDER"
        );
        return dsResult;
    }

    /**
     * 根据工作流实例id，查询出工作流实例的运行详情
     * @param projectCode
     * @param flowInstanceId
     * @return
     */
    public DsResult queryFlowInstanceDetail(long projectCode, Integer flowInstanceId){
        DsResult dsResult = dsFeign.queryProcessInstanceById(token, projectCode, flowInstanceId);
        return dsResult;
    }


}
