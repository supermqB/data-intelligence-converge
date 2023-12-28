package com.lrhealth.data.converge.common.util.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.lrhealth.data.converge.common.exception.FileMergeException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;


/**
 * 文件处理辅助类
 * @author zhaohui
 * @version 1.0
 */
public class FileUtils {

    /**
     * 当前目录路径
     */
    public static String currentWorkDir = System.getProperty("user.dir") + "\\";

    /**
     * 左填充
     *
     * @param str
     * @param length
     * @param ch
     * @return
     */
    public static String leftPad(String str, int length, char ch) {
        if (str.length() >= length) {
            return str;
        }
        char[] chs = new char[length];
        Arrays.fill(chs, ch);
        char[] src = str.toCharArray();
        System.arraycopy(src, 0, chs, length - src.length, src.length);
        return new String(chs);

    }

    /**
     * 删除文件
     *
     * @param fileName
     *            待删除的完整文件名
     * @return
     */
    public static boolean delete(String fileName) {
        boolean result = false;
        File f = new File(fileName);
        if (f.exists()) {
            result = f.delete();

        } else {
            result = true;
        }
        return result;
    }

    /***
     * 递归获取指定目录下的所有的文件（不包括文件夹）
     *
     * @param dirPath
     * @return
     */
    public static ArrayList<File> getAllFiles(String dirPath) {
        File dir = new File(dirPath);

        ArrayList<File> files = new ArrayList<File>();

        if (dir.isDirectory()) {
            File[] fileArr = dir.listFiles();
            for (int i = 0; i < fileArr.length; i++) {
                File f = fileArr[i];
                if (f.isFile()) {
                    files.add(f);
                } else {
                    files.addAll(getAllFiles(f.getPath()));
                }
            }
        }
        return files;
    }

    /**
     * 获取指定目录下的所有文件(不包括子文件夹)
     *
     * @param dirPath
     * @return
     */
    public static ArrayList<File> getDirFiles(String dirPath) {
        File path = new File(dirPath);
        File[] fileArr = path.listFiles();
        ArrayList<File> files = new ArrayList<File>();

        for (File f : fileArr) {
            if (f.isFile()) {
                files.add(f);
            }
        }
        return files;
    }

    /**
     * 获取指定目录下特定文件后缀名的文件列表(不包括子文件夹)
     *
     * @param dirPath
     *            目录路径
     * @param suffix
     *            文件后缀
     * @return
     */
    public static ArrayList<File> getDirFiles(String dirPath,
                                              final String suffix) {
        File path = new File(dirPath);
        File[] fileArr = path.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            String lowerSuffix = suffix.toLowerCase();
            return lowerName.endsWith(lowerSuffix);
        });
        ArrayList<File> files = new ArrayList<>();
        if (fileArr == null || fileArr.length < 1){
            return files;
        }
        for (File f : fileArr) {
            if (f.isFile()) {
                files.add(f);
            }
        }
        return files;
    }

    /**
     * 读取文件内容
     *
     * @param fileName
     *            待读取的完整文件名
     * @return 文件内容
     * @throws IOException
     */
    public static String read(String fileName) throws IOException {
        File f = new File(fileName);
        FileInputStream fs = new FileInputStream(f);
        String result = null;
        byte[] b = new byte[fs.available()];
        fs.read(b);
        fs.close();
        result = new String(b);
        return result;
    }

    /**
     * 写文件
     *
     * @param fileName
     *            目标文件名
     * @param fileContent
     *            写入的内容
     * @return
     * @throws IOException
     */
    public static boolean write(String fileName, String fileContent)
            throws IOException {
        boolean result = false;
        File f = new File(fileName);
        FileOutputStream fs = new FileOutputStream(f);
        byte[] b = fileContent.getBytes();
        fs.write(b);
        fs.flush();
        fs.close();
        result = true;
        return result;
    }

    /**
     * 追加内容到指定文件
     *
     * @param fileName
     * @param fileContent
     * @return
     * @throws IOException
     */
    public static boolean append(String fileName, String fileContent)
            throws IOException {
        boolean result = false;
        File f = new File(fileName);
        if (f.exists()) {
            RandomAccessFile rFile = new RandomAccessFile(f, "rw");
            byte[] b = fileContent.getBytes();
            long originLen = f.length();
            rFile.setLength(originLen + b.length);
            rFile.seek(originLen);
            rFile.write(b);
            rFile.close();
        }
        result = true;
        return result;
    }

    /**
     * 合并文件
     *
     * @param threadPoolTaskExecutor
     * @param dirPath                拆分文件所在目录名
     * @param partFileSuffix         拆分文件后缀名
     * @param partFileSize           拆分文件的字节数大小
     * @param mergeFileName          合并后的文件名
     * @param aesKey                 加密
     * @throws IOException
     */
    public void mergePartFiles(Executor executor, String dirPath, String partFileSuffix,
                               int partFileSize, String mergeFileName, byte[] aesKey) throws Exception {
        ArrayList<File> partFiles = FileUtils.getDirFiles(dirPath,
                partFileSuffix);
        partFiles.sort(new FileComparator());
        FileUtils.delete(mergeFileName);
        RandomAccessFile randomAccessFile = new RandomAccessFile(mergeFileName,
                "rw");
        randomAccessFile.close();

        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < partFiles.size(); i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(new MergeRunnable(dirPath,
                            i * partFileSize, mergeFileName, partFiles.get(i), aesKey),executor)
                    .exceptionally(e -> {
                        throw new FileMergeException("文件合并异常: " + mergeFileName + "\n"
                                + e.getMessage() + "\n"
                                + Arrays.toString(e.getStackTrace()));
                    });
            futureList.add(future);
        }

        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                .thenApply(e -> futureList.stream().map(CompletableFuture::join)
                        .collect(Collectors.toList())).join();

    }

    /**
     * 根据文件名，比较文件
     *
     * @author yjmyzz@126.com
     *
     */
    private class FileComparator implements Comparator<File> {
        public int compare(File o1, File o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    /**
     * 合并处理Runnable
     *
     * @author yjmyzz@126.com
     *
     */
    private class MergeRunnable implements Runnable {
        long startPos;
        String mergeFileName;
        File partFile;
        byte[] aesKey;
        String destPath;

        public MergeRunnable(String destPath, long startPos, String mergeFileName, File partFile, byte[] aesKey) {
            this.destPath = destPath;
            this.startPos = startPos;
            this.mergeFileName = mergeFileName;
            this.partFile = partFile;
            this.aesKey = aesKey;
        }

        public void run() {
            RandomAccessFile rFile;
            try {
                rFile = new RandomAccessFile(mergeFileName, "rw");
                rFile.seek(startPos);
                FileInputStream fs = new FileInputStream(partFile);
                AES aes = SecureUtil.aes(aesKey);
                aes.decrypt(fs, Files.newOutputStream(Paths.get(destPath + partFile.getName() + ".zip")),true);
                fs.close();
                partFile.delete();
                File unzip = ZipUtil.unzip(destPath + partFile.getName() + ".zip");
                FileInputStream fileInputStream =
                        new FileInputStream(FileUtil.file(unzip.getPath() +
                                File.separator + partFile.getName() + ".temp"));

                byte[] b = new byte[2048];
                int len;
                while ((len = fileInputStream.read(b)) != -1) {
                    rFile.write(b, 0, len);
                }

                rFile.close();
                fileInputStream.close();
                FileUtils.delete(destPath +unzip.getName());
                FileUtils.delete(destPath + partFile.getName()+ ".zip");
                FileUtil.del(destPath + partFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
