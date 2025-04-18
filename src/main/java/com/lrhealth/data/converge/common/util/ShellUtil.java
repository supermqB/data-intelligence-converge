package com.lrhealth.data.converge.common.util;

import cn.hutool.log.Log;

import java.io.InputStream;
import java.util.List;

import static cn.hutool.core.text.CharSequenceUtil.*;
import static cn.hutool.core.util.PrimitiveArrayUtil.isEmpty;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * <p>
 * Shell脚本处理
 * </p>
 *
 * @author lr
 * @since 2023/7/27
 */
public class ShellUtil {
    private static final Log log = Log.get(ShellUtil.class);

    private ShellUtil() {
    }

    /**
     * 执行脚本
     *
     * @param pathOrCommand 命令
     * @return 执行结果
     */
    public static String execCommand(List<String> pathOrCommand) {
        log.info("start exec command:{}", String.join(SPACE, pathOrCommand));
        String line = EMPTY;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            Process ps = processBuilder.command(pathOrCommand).start();

            InputStream inputStream = ps.getInputStream();
            byte[] bytes = new byte[1024];
            while (inputStream.read(bytes) != -1) {
                line = isEmpty(bytes) ? EMPTY : new String(bytes, UTF_8);
            }
            line = replace(line, "\0", EMPTY);
            line = replace(line, "\n", EMPTY);
            log.info("exec command log:{}", line);
            inputStream.close();

            // 接收脚本echo最后一次打印的数据,正常数据
            InputStream errorStream = ps.getErrorStream();
            byte[] errorBytes = new byte[1024];
            String errorLine = EMPTY;
            while (errorStream.read(errorBytes) != -1) {
                errorLine = isEmpty(errorBytes) ? EMPTY : new String(errorBytes, UTF_8);
            }
            errorLine = replace(errorLine, "\0", EMPTY);
            errorLine = replace(errorLine, "\n", EMPTY);
            if (isNotBlank(errorLine)) {
                log.error("exec command error log:{}", errorLine);
                line = errorLine;
            }
            errorStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Shell exec error:", e);
            return e.getMessage();
        }
        return line;
    }
}
