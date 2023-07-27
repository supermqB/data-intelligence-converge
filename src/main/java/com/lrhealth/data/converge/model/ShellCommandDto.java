package com.lrhealth.data.converge.model;

import lombok.Data;

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
    private String command;
}
