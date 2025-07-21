package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 经过整合的cdc落库类
 * @author jinmengyu
 * @date 2024-12-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpSqlDto {

    private String operation;

    private String sqlTemplate;

    private List<Map<String, Object>> valueMapList;

}
