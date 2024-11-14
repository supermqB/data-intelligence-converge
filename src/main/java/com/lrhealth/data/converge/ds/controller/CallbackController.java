package com.lrhealth.data.converge.ds.controller;

import com.alibaba.fastjson2.JSON;
import com.lrhealth.data.common.result.ResultBase;
import com.lrhealth.data.converge.ds.biz.DsFlowBiz;
import com.lrhealth.data.converge.ds.dto.FlowInstanceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * ds回调汇聚，操作工作流相关
 */
@RestController
@RequestMapping(value = "/ds")
@Slf4j
public class CallbackController {

    @Resource
    private DsFlowBiz dsFlowBiz;

    /**
     * ds回调汇聚，组装工作流实例运行的结果，插入conv_dolpsche_flow_run_detail
     * @return
     */
    @PostMapping(value = "/flow/callback")
    public ResultBase<String> flowCallback(@RequestBody FlowInstanceDto dto) {
        log.info("flowCallback,request={}", JSON.toJSONString(dto));
        dsFlowBiz.flowCallback(dto);
        return ResultBase.success("工作流实例回调完成");
    }

}
