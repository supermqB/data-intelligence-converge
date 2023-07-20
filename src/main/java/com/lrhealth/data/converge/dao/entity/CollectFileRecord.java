package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 采集文件信息
 * </p>
 *
 * @author jinmengyu
 * @since 2023-07-20
 */
@Data
@TableName("conv_collect_file_record")
public class CollectFileRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 最后修改时间
     */
    private Long lastModified;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
