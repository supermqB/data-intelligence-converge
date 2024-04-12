package com.lrhealth.data.converge.model.dto;

import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2023-11-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMessageDTO {

    private Integer taskResultId;

    private TunnelCMEnum tunnelCMEnum;

    private String tableName;

    private String feStoredFileName;

    private String storedPath;

    private Long dataSize;

    private Long dataItemCount;

    private Integer taskId;
}
