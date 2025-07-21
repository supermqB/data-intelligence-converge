package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveInterfaceDTO {


    private String requestMethod;

    private String requestUrl;

    private String requestParam;

    private String authentication;

    private String requestBody;

    private String createTime;

    private String updateTime;

    private String dataPath;

    private Integer delFlag;

}
