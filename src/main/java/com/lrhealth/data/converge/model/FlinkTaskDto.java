package com.lrhealth.data.converge.model;

import lombok.Data;

/**
 * flinkCDS采集数据新建xds
 * @author jinmengyu
 * @date 2023-07-28
 */
@Data
public class FlinkTaskDto {

    private Long xdsId;

    private Long convergeTime;

    private Long convergeConfigId;

}
