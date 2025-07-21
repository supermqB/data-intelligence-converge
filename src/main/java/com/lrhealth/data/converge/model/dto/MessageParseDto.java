package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author jinmengyu
 * @date 2024-08-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageParseDto {

    /**
     * 表名称
     */
    private String readTable;

    /**
     * 动作
     * c-create; u-update;d-delete
     */
    private String operation;

    /**
     * 修改前数据
     */
    private Map<String, Object> preValue;

    /**
     * 修改后数据
     */
    private Map<String, Object> postValue;


}
