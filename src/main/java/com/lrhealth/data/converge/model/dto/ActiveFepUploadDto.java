package com.lrhealth.data.converge.model.dto;

import lombok.Data;

/**
 * @author jinmengyu
 * @date 2023-11-15
 */
@Data
public class ActiveFepUploadDto {

    private String ip;

    private Integer port;

    private FrontendStatusDto frontendStatusDto;

}
