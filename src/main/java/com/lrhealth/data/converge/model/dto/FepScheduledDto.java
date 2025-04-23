package com.lrhealth.data.converge.model.dto;

import com.lrhealth.data.converge.model.fep.ReaderConfigDTO;
import com.lrhealth.data.converge.model.fep.WriterConfigDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinmengyu
 * @date 2023-12-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FepScheduledDto {

    // tunnel id
    private Long tunnelId;

    // old task id or a new one
    private Integer taskId;

    // reader config
    private ReaderConfigDTO readerConfigDTO;

    // writer config
    private WriterConfigDTO writerConfigDTO;
}
