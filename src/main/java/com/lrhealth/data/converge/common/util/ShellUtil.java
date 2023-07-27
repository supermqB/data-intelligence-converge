package com.lrhealth.data.converge.common.util;

import cn.hutool.log.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.List;

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

    private ShellUtil() {
    }

    public static void createShellFile(String filePath, String... str) {
        try {

            File sh = new File(filePath);
            if (sh.exists()) {
                sh.delete();
            }
            sh.createNewFile();
            sh.setExecutable(true);
            FileWriter writer = new FileWriter(sh);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            for (int i = 0; i < str.length; i++) {
                bufferedWriter.write(str[i]);
                if (i < str.length - 1) {
                    bufferedWriter.newLine();
                }
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行脚本
     *
     * @param pathOrCommand 命令
     * @return 执行结果
     */
    public static String execCommand(List<String> pathOrCommand) {
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
