package com.lrhealth.data.converge.model.dto;

import lombok.Data;

@Data
public class DbTableMergeDTO {
    private String tunnelId;
    private String dsId;
    private String dbName;
    private String tbName;
}