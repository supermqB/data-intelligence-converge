package com.lrhealth.data.converge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jinmengyu
 * @date 2025-04-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnValueUpDTO {

    private Long tableId;

    private String tableName;

    private Long columnId;

    private String columnName;


    private List<DictValueDTO> valueDTOList;

}
