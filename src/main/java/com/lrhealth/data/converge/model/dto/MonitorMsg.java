package com.lrhealth.data.converge.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;

import java.util.Date;

/**
 * <p>
 * 监控消息
 * </p>
 *
 * @author lr
 * @since 2023/11/28 15:56
 */
@Data
public class MonitorMsg {
    /**
     * 来源应用IP
     */
    private String sourceIp;
    /**
     * 来源应用IP
     */
    private String sourcePort;
    /**
     * 消息类型
     *
     * @see MsgTypeEnum
     */
    private String msgType;
    /**
     * 消息
     */
    private String msg;
    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sendTime;

    /**
     * 消息类型
     */
    @Getter
    public enum MsgTypeEnum {
        /**
         * 消息类型
         */
        FEP_STA("1", "前置机程序状态监测"),
        BATCH_TASK("2", "批量采集任务执行"),
        CDC_STA("3", "CDC程序状态监测"),
        CDC_TASK("4", "前置机程序状态监测"),

        ;
        private final String msgTypeCode;
        private final String msgTypeDesc;

        MsgTypeEnum(String msgTypeCode, String msgTypeDesc) {
            this.msgTypeCode = msgTypeCode;
            this.msgTypeDesc = msgTypeDesc;
        }

    }
}
