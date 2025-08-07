package com.lrhealth.data.converge.model.dto;

import lombok.Data;

import java.util.HashMap;

@Data
public class PreFileStatusDto {

    private String status;

    private String oriFileName;

    private String path;

    private HashMap<String, Integer> partFileMap;

    public PreFileStatusDto(String status, String oriFileName, String path, HashMap<String, Integer> partFileMap) {
        this.status = status;
        this.oriFileName = oriFileName;
        this.path = path;
        this.partFileMap = partFileMap;
    }
}
