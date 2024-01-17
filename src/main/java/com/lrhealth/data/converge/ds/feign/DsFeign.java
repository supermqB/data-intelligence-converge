package com.lrhealth.data.converge.ds.feign;

import com.lrhealth.data.converge.ds.dto.DsResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "DSFeignClient",url = "${ds.host}")
@Component
public interface DsFeign {

    @PostMapping(value = "/projects")
    DsResult createProject(@RequestHeader(name = "token") String token,
                           @RequestParam("projectName") String projectName,
                           @RequestParam(value = "description", required = false) String description);

    @PostMapping("/projects/{projectCode}/process-definition")
    DsResult createProcessDefinition(@RequestHeader(name = "token") String token,
                                     @PathVariable long projectCode,
                                      @RequestParam(value = "name", required = true) String name,
                                      @RequestParam(value = "description", required = false) String description,
                                      @RequestParam(value = "locations", required = false) String locations,
                                      @RequestParam(value = "taskRelationJson", required = true) String taskRelationJson,
                                      @RequestParam(value = "taskDefinitionJson", required = true) String taskDefinitionJson);

    @PostMapping(value = "/projects/{projectCode}/process-definition/{code}/release")
    DsResult releaseProcessDefinition(@RequestHeader(name = "token") String token,
                                      @PathVariable long projectCode,
                                       @PathVariable(value = "code", required = true) long code,
                                       @RequestParam(value = "releaseState", required = true) String releaseState);

    @PostMapping(value = "/projects/{projectCode}/executors/start-process-instance")
    DsResult startProcessInstance(@RequestHeader(name = "token") String token,
                                       @PathVariable long projectCode,
                                       @RequestParam(value = "processDefinitionCode") long processDefinitionCode,
                                       @RequestParam(value = "scheduleTime") String scheduleTime,
                                       @RequestParam(value = "failureStrategy") String failureStrategy,
//                                       @RequestParam(value = "startNodeList", required = false) String startNodeList,
                                       @RequestParam(value = "taskDependType", required = false) String taskDependType,
                                       @RequestParam(value = "execType", required = false) String execType,
                                       @RequestParam(value = "warningType") String warningType,
//                                       @RequestParam(value = "warningGroupId", required = false) Integer warningGroupId,
                                       @RequestParam(value = "runMode", required = false) String runMode,
                                       @RequestParam(value = "processInstancePriority", required = false) String processInstancePriority,
//                                       @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
//                                       @RequestParam(value = "tenantCode", required = false, defaultValue = "default") String tenantCode,
//                                       @RequestParam(value = "environmentCode", required = false, defaultValue = "-1") Long environmentCode,
//                                       @RequestParam(value = "timeout", required = false) Integer timeout,
//                                       @RequestParam(value = "startParams", required = false) String startParams,
//                                       @RequestParam(value = "expectedParallelismNumber", required = false) Integer expectedParallelismNumber,
//                                       @RequestParam(value = "dryRun", defaultValue = "0", required = false) int dryRun,
//                                       @RequestParam(value = "testFlag", defaultValue = "0") int testFlag,
                                       @RequestParam(value = "complementDependentMode", required = false) String complementDependentMode,
//                                       @RequestParam(value = "version", required = false) Integer version,
//                                       @RequestParam(value = "allLevelDependent", required = false, defaultValue = "false") boolean allLevelDependent,
                                       @RequestParam(value = "executionOrder", required = false) String executionOrder);


    @GetMapping(value = "/projects/{projectCode}/process-instances/{id}")
    DsResult queryProcessInstanceById(@RequestHeader(name = "token") String token,
                                      @PathVariable long projectCode,
                                      @PathVariable("id") Integer id);

}
