package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

@Data
public class ConvActiveInterfaceConfig {
    private Integer id;

    private Long tunnelId;

    private String requestMethod;

    private String requestUrl;

    private String requestParam;

    private String authentication;

    private String requestBody;

    private String dataPath;


    @TableLogic
    private Integer delFlag;
}
