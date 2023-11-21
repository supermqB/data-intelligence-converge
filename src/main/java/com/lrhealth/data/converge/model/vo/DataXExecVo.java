package com.lrhealth.data.converge.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2023-11-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataXExecVo {

    /**
     * 管道id
     */
    private Long tunnelId;

    private String jdbcUrl;

    private String dbUsername;

    private String dbPassword;

}
