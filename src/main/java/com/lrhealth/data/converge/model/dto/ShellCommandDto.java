package com.lrhealth.data.converge.model.dto;

import lombok.Data;

import java.util.List;

/**
 * <p>
 * shell脚本执行
 * </p>
 *
 * @author lr
 * @since 2023/7/27
 */
@Data
public class ShellCommandDto {
    /**
     * 命令行
     */
    private List<String> command;
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 文件名称
     */
    private String fileName;
}
