package com.lrhealth.data.converge.scheduled.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2023-08-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TunnelMessageDTO {

    /**
     *  管道id
     */
    private Long id;

    /**
     * 管道名称
     */
    private String name;

    /**
     * 动作:add/update/delete
     */
    private String action;

    /**
     * 汇聚方式：1-库表模式，2-日志模式（CDC），3-文件模式，4-接口模式，5-队列模式
     */
    private String convergeMethod;

    /**
     * 任务调度cron表达式
     */
    private String cronStr;

    /**
     * 库表信息
     */
    private JdbcInfoDto jdbcInfoDto;

    /**
     * 文件是否加密：0-不加密，1-加密
     */
    private String encryptionFlag;

    /**
     *  文件是否压缩
     */
    private String zipFlag;

    /**
     * 数据分片大小,使用bytes
     */
    private Long dataShardSize;

    private Integer status;

    /**
     * 文件地址
     */
    private String fileModeCollectDir;

    /**
     * 文件采集范围
     */
    private String collectRange;

    private String mqModeTopicName;

}
