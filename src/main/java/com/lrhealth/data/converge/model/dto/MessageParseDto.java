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

    private String readTable;

    private String operation;

    private Map<String, Object> preValue;

    private Map<String, Object> postValue;


}
