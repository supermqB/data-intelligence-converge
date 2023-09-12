package com.lrhealth.data.converge.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 队列服务http请求统一返回格式
 *
 * @author jinmengyu
 * @since 2023-07-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DolphinSchedulerReturnVO {
    /**
     * 自定义编码
     */
    private String code;

    /**
     * 返回结果的实体类
     */
    private Object data;
}
