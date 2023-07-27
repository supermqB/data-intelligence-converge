package com.lrhealth.data.converge.common.util;

import cn.hutool.log.Log;

import java.io.InputStream;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
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

    public static String execCommand(String pathOrCommand) {
        String line = EMPTY;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            Process ps = processBuilder.command(pathOrCommand).start();

            InputStream inputStream = ps.getInputStream();
            byte[] bytes = new byte[1024];
            while (inputStream.read(bytes) != -1) {
                line = new String(bytes, UTF_8);
            }
            inputStream.close();

            // 接收脚本echo最后一次打印的数据,正常数据
            InputStream errorStream = ps.getErrorStream();
            bytes = new byte[1024];
            while (errorStream.read(bytes) != -1) {
                line = new String(bytes, UTF_8);
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
