package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author jinmengyu
 * @date 2024-10-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("convMessageQueueConfigServiceg")
public class ConvMonitorLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 监控类型
     * MonitorMsg.MsgTypeEnum
     */
    private String monitorType;

    /**
     * 监控信息
     */
    private String monitorMsg;

    /**
     * 落库时间
     */
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 来源应用IP
     */
    private String sourceIp;
    /**
     * 来源应用IP
     */
    private Integer sourcePort;

    private Integer status;

}
