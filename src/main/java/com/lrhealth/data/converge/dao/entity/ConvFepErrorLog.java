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
 * @date 2024-10-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("conv_fep_error_log")
public class ConvFepErrorLog {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Integer id;

    private String ip;

    private Integer port;

    private String errorMsg;

    private String stacktrace;

    private LocalDateTime logTime;

    private String orgCode;
}
