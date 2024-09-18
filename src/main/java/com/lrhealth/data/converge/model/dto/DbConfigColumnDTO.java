package com.lrhealth.data.converge.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author lei
 * @Date: 2024/03/06/11:21
 */
@Data
public class DbConfigColumnDTO {

    private String partitionType;

    private List<String> partitionKey;

    private List<String> bucketKey;

    private Integer bucketNum;

}
