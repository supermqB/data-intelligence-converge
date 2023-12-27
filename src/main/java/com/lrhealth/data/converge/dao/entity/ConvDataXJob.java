package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author jinmengyu
 * @since 2023-09-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("conv_datax_config")
public class ConvDataXJob implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tunnelId;

    private String tableName;

    private String jsonPath;

    private String jobMode;

    private String createBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer delFlag;

    private String sqlQuery;

    private String seqFields;
}
