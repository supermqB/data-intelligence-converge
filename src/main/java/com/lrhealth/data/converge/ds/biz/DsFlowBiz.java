package com.lrhealth.data.converge.ds.biz;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.converge.ds.dao.entity.ConvDolpscheFlowRunDetail;
import com.lrhealth.data.converge.ds.dao.entity.ConvDolpscheRel;
import com.lrhealth.data.converge.ds.dao.service.IConvDolpscheFlowRunDetailService;
import com.lrhealth.data.converge.ds.dao.service.IConvDolpscheRelService;
import com.lrhealth.data.converge.ds.dto.DsResult;
import com.lrhealth.data.converge.ds.dto.FlowInstanceDto;
import com.lrhealth.data.converge.ds.feign.DsFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

@Component
@Slf4j
public class DsFlowBiz {

    private static SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd HHmmss");
    @Resource
    private IConvDolpscheRelService iConvDolpscheRelService;

    @Resource
    private IConvDolpscheFlowRunDetailService iConvDolpscheFlowRunDetailService;

    @Resource
    private DsFeignClient dsFeignClient;

    /**
     * 组装工作流实例运行的结果，插入conv_dolpsche_flow_run_detail
     * @param dto
     */
    public void flowCallback(FlowInstanceDto dto){
        DsResult dsResult = dsFeignClient.queryFlowInstanceDetail(dto.getProjectCode(),dto.getFlowInstanceId());
        if(!dsResult.isSuccess()){
            log.error("flowCallback，获取工作流实例的运行详情失败，dto={}", JSON.toJSONString(dto));
            return;
        }
        // 生成ConvDolpscheFlowRunDetail对象
        ConvDolpscheFlowRunDetail flowRunDetail = buildConvDolpscheFlowRunDetail(dto, dsResult);
        if(flowRunDetail != null){
            // 插入 conv_dolpsche_flow_run_detail
            iConvDolpscheFlowRunDetailService.save(flowRunDetail);
        }
    }

    /**
     * 生成ConvDolpscheFlowRunDetail对象
     * 1.从iConvDolpscheRelService中取org_code， 2.从dsResult取其他字段
     * @param dto
     * @param dsResult
     * @return
     */
    private ConvDolpscheFlowRunDetail buildConvDolpscheFlowRunDetail(FlowInstanceDto dto,DsResult dsResult){
        try {
            //        long projectCode =  Long.valueOf(String.valueOf(dsMap.get("projectCode")));
            LambdaQueryWrapper<ConvDolpscheRel> relWrapper = new LambdaQueryWrapper<>();
            relWrapper.eq(ConvDolpscheRel::getDsProjectCode,dto.getProjectCode()).eq(ConvDolpscheRel::getDelFlag,0);
            List<ConvDolpscheRel> convDolpscheRelList = iConvDolpscheRelService.list(relWrapper);
            if(CollectionUtils.isEmpty(convDolpscheRelList)){
                log.error("startDsFlow error,汇聚和ds关系表无记录，请检查！");
                return null;
            }

            String orgCode = convDolpscheRelList.get(0).getConvOrgCode();
            LinkedHashMap dsMap = (LinkedHashMap)dsResult.getData();
            String projectCode = String.valueOf(dsMap.get("projectCode"));
            String flowCode = String.valueOf(dsMap.get("processDefinitionCode"));
            String flowName = dto.getFlowName();
            String flowInstanceName = String.valueOf(dsMap.get("name"));
            Integer flowInstanceId = dto.getFlowInstanceId();
            String state = String.valueOf(dsMap.get("state"));
            Integer flowInstanceStatus = 2;
            if(state.equals("SUCCESS")){
                flowInstanceStatus = 1;
            }
            Date flowInstanceStartTime = SDF.parse(String.valueOf(dsMap.get("startTime")));
            Date flowInstanceEndTime = SDF.parse(String.valueOf(dsMap.get("endTime")));
            return ConvDolpscheFlowRunDetail.builder().orgCode(orgCode).projectCode(projectCode).projectName(projectCode).
                    flowCode(flowCode).flowName(flowName).flowInstanceId(flowInstanceId).flowInstanceName(flowInstanceName).
                    flowInstanceStatus(flowInstanceStatus).flowInstanceStartTime(flowInstanceStartTime).
                    flowInstanceEndTime(flowInstanceEndTime).build();
        }catch (Exception e){
            log.error("buildConvDolpscheFlowRunDetail error",e);
            return null;
        }
    }
}
