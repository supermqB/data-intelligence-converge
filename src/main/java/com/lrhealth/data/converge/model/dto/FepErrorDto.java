package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2024-10-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FepErrorDto {

    String ip;

    String port;

    String errorMsg;

    String stacktrace;

    String orgCode;
}
