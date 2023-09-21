package com.lrhealth.data.converge.scheduled.model.dto;

import lombok.Data;

import java.util.HashMap;

/**
 * @author zhaohui
 * @version 1.0
 * @description: TODO
 * @date 2023/9/14 10:10
 */
@Data
public class PreFileStatusDto {

    private String status;

    private String oriFileName;

    private String path;

    private HashMap<String,Integer> partFileMap;

    public PreFileStatusDto(String status, String oriFileName, String path, HashMap<String, Integer> partFileMap) {
        this.status = status;
        this.oriFileName = oriFileName;
        this.path = path;
        this.partFileMap = partFileMap;
    }
}
