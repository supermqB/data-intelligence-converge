package com.lrhealth.data.converge.model.dto;

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
     * 系统编码
     */
    private String sysCode;

    /**
     * 管道动作
     * 动作:add/update/delete
     * 目前在汇聚主动调用前置机时使用
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
     * cron采集时间范围提前的时间差
     * 单位：毫秒
     */
    private Integer timeDif;

    /**
     * 库表模式-库表信息
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

    /**
     * 管道状态
     */
    private Integer status;


    /**
     * 管道依赖
     */
    private Long dependenceTunnelId;

    /**
     * 文件采集
     */
    private FileCollectInfoDto fileCollectInfoDto;

}
