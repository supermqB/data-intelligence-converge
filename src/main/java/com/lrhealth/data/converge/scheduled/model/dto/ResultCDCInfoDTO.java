package com.lrhealth.data.converge.scheduled.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yuanbaiyu
 * @since 2023/10/9 16:27
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResultCDCInfoDTO {


    private Integer taskId;

    private String flinkJobId;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 记录条数
     */
    private Integer dataCount;
    /**
     * 插入条数
     */
    private Integer addCount;
    /**
     * 更新条数
     */
    private Integer updateCount;
    /**
     * 删除条数
     */
    private Integer deleteCount;

}
