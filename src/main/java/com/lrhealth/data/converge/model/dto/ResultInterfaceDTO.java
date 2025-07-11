package com.lrhealth.data.converge.model.dto;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultInterfaceDTO extends TaskInfoKafkaDto{

    private String tableName;

    private Integer dataItemCount;

    private Integer status;
}
