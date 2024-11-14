package com.lrhealth.data.converge.ds.feign;

import com.lrhealth.data.converge.ds.dto.DsResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
