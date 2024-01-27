package com.lrhealth.data.converge.model.dto;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
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
public class MonitorMsg implements Serializable {
    /**
     * 状态
     */
    private Boolean status;

    /**
     * 管道ID
     */
    private Long tunnelId;
    /**
     * 数据源ID
     */
    private Integer dsId;
    /**
     * 机构编码
     */
    private String orgCode;
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
    private Date sendTime;

    /**
     * 消息类型
     */
    @Getter
    public enum MsgTypeEnum {
        FEP_STA("1", "前置机程序状态监测"),
        BATCH_TASK("2", "批量采集任务执行"),
        CDC_STA("3", "CDC程序状态监测"),
        CDC_TASK("4", "CDC采集任务执行"),
        READER_DB_CHECK("5", "目标数据库连接检查"),
        WRITER_DB_CHECK("6", "中心数据库连接检查");
        private final String msgTypeCode;
        private final String msgTypeDesc;

        MsgTypeEnum(String msgTypeCode, String msgTypeDesc) {
            this.msgTypeCode = msgTypeCode;
            this.msgTypeDesc = msgTypeDesc;
        }

        public static String getDescByCode(String code) {
            for (MsgTypeEnum enumValue : MsgTypeEnum.values()) {
                if (enumValue.getMsgTypeCode().equals(code)) {
                    return enumValue.getMsgTypeDesc();
                }
            }
            return null;
        }

    }
}
