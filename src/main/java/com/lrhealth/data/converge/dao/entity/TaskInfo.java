package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 汇聚任务信息
 * </p>
 *
 * @author jinmengyu
 * @since 2023-07-20
 */
@Data
@TableName("conv_task_info")
public class TaskInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long taskId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件上传路径
     */
    private String filePath;

    /**
     * 文件上传速度
     */
    private Integer speed;

    /**
     * 机构名称
     */
    private String orgName;

    /**
     * 汇聚方式： OFF_LINE离线；REAL_TIME实时
     */
    private String convergeWay;

    /**
     * 0-不压缩，1-压缩
     */
    private String zipFlag;

    /**
     * 开始时间
     */
    private Integer startTime;

    /**
     * 结束时间
     */
    private Integer endTime;

    /**
     * 机构编码
     */
    private String orgCode;
}
