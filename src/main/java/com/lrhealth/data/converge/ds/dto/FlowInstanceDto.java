package com.lrhealth.data.converge.ds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FlowInstanceDto {
    /**
     * ds中使用全局参数：${system.project.code} 传递过来
     */
    private long projectCode;
    /**
     * ds中使用全局参数：${system.workflow.instance.id} 传递过来
     */
    private Integer flowInstanceId;
    /**
     * ds中使用全局参数：${system.workflow.definition.name} 传递过来
     */
    private String flowName;
}
