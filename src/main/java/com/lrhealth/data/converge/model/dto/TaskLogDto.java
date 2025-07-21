package com.lrhealth.data.converge.model.dto;

import lombok.*;

/**
 * @author jinmengyu
 * @date 2023-09-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskLogDto extends TaskInfoKafkaDto{

    private Long logId;

    private String logDetail;

    private String logTime;

}
