package com.lrhealth.data.converge.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author gaoshuai
 * @since 2025-07-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ConvFileCollect implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Long tunnelId;

    private String fileModeCollectDir;

    private Integer structuredDataFlag;

    private String fileCollectRange;

    private String fileSuffix;

    private Integer fileStorageMode;

    /**
     * 0：全量，文件夹下的所有文件，1：增量，新增文件
     */
    private Integer incrFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Integer delFlag;


}
