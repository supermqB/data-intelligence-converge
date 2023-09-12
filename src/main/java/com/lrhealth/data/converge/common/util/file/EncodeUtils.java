package com.lrhealth.data.converge.common.util.file;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.BitSet;

import static cn.hutool.core.text.StrPool.SLASH;

/**
 * 编码工具类，主要用于识别UTF8、UTF8 BOM、GBK
 * 2022/08/18
 */
@Slf4j
public class EncodeUtils {
    private EncodeUtils(){}

    private static final int BYTE_SIZE = 8;
    public static final String CODE_UTF8 = "UTF-8";
    public static final String UNICODE = "Unicode";
    public static final String CODE_UTF8_BOM = "UTF-8_BOM";
    public static final String CODE_GBK = "GBK";
    public static final String BLANK = "";
 
    /**
     * 通过文件全名称获取编码集名称
     *
     * @param fullFileName
     * @param ignoreBom
     * @return
     * @throws Exception
     */
    public static String getEncode(String fullFileName, boolean ignoreBom) {

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fullFileName))){
            return getEncode(bis, ignoreBom);
        } catch (Exception e) {
            log.error("EncodeUtils.getEncode方法异常:",e);
            return BLANK;
        }
    }
 
    /**
     * 通过文件缓存流获取编码集名称
     *
     * @param in
     * @return
     * @throws Exception
     */
    public static String getEncode(@NonNull InputStream in, boolean ignoreBom) {
        String encodeType = "";
        try (BufferedInputStream bis = new BufferedInputStream(in)){

            bis.mark(0);
            byte[] head = new byte[3];
            bis.read(head);
            if (head[0] == -1 && head[1] == -2) {
                encodeType = "UTF-16";
            } else if (head[0] == -2 && head[1] == -1) {
                encodeType = UNICODE;
            } else if (head[0] == -17 && head[1] == -69 && head[2] == -65) {
                //带BOM
                if (ignoreBom) {
                    encodeType = CODE_UTF8;
                } else {
                    encodeType = CODE_UTF8_BOM;
                }
            } else if (UNICODE.equals(encodeType)) {
                encodeType = "UTF-16";
            } else if (isUTF8(bis)) {
                encodeType = CODE_UTF8;
            } else {
                encodeType = CODE_GBK;
            }
            log.info("result encode type : " + encodeType);
        } catch (Exception e) {
            log.error("通过文件缓存流获取编码集名称方法异常:",e);
            return BLANK;
        }

        return encodeType;
    }
 
    /**
     * 是否是无BOM的UTF8格式，不判断常规场景，只区分无BOM UTF8和GBK
     *
     * @param
     * @return
     */
    private static boolean isUTF8(@NonNull BufferedInputStream bis) {
        try {
            bis.reset();
            //读取第一个字节
            int code = bis.read();
            do {
                BitSet bitSet = convert2BitSet(code);
                //判断是否为单字节
                if (bitSet.get(0)) {
                    //多字节时，再读取N个字节
                    if (!checkMultiByte(bis, bitSet)) {
                        //未检测通过,直接返回
                        return false;
                    }
                } else {
                    //单字节时什么都不用做，再次读取字节
                }
                code = bis.read();
            } while (code != -1);
        } catch (Exception e) {
            log.error("isUTF8方法异常:",e);
            return false;
        } finally {
            try {
                bis.close();
            } catch (IOException e) {
                log.error("文件缓存流关闭异常:", e);
                e.printStackTrace();
            }
        }
        return true;
    }
 
    /**
     * 检测多字节，判断是否为utf8，已经读取了一个字节
     *
     * @param bis
     * @param bitSet
     * @return
     */
    private static boolean checkMultiByte(@NonNull BufferedInputStream bis, @NonNull BitSet bitSet) {
        try {
            int count = getCountOfSequential(bitSet);
            byte[] bytes = new byte[count - 1];
            //已经读取了一个字节，不能再读取
            bis.read(bytes);
            for (byte b : bytes) {
                if (!checkUtf8Byte(b)) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("checkMultiByte方法异常:",e);
            return false;
        }
        return true;
    }
 
    /**
     * 检测单字节，判断是否为utf8
     *
     * @param b
     * @return
     */
    private static boolean checkUtf8Byte(byte b) {
        BitSet bitSet = convert2BitSet(b);
        return bitSet.get(0) && !bitSet.get(1);
    }
 
    /**
     * 检测bitSet中从开始有多少个连续的1
     *
     * @param bitSet
     * @return
     */
    private static int getCountOfSequential(@NonNull BitSet bitSet) {
        int count = 0;
        for (int i = 0; i < BYTE_SIZE; i++) {
            if (bitSet.get(i)) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }
 
 
    /**
     * 将整形转为BitSet
     *
     * @param code
     * @return
     */
    private static BitSet convert2BitSet(int code) {
        BitSet bitSet = new BitSet(BYTE_SIZE);
 
        for (int i = 0; i < BYTE_SIZE; i++) {
            int tmp3 = code >> (BYTE_SIZE - i - 1);
            int tmp2 = 0x1 & tmp3;
            if (tmp2 == 1) {
                bitSet.set(i);
            }
        }
        return bitSet;
    }
 
    /**
     * 将一指定编码的文件转换为另一编码的文件
     *
     * @param oldFullFileName  完整路径名
     * @param oldCharsetName
     * @param newFullFileName  完整路径名
     * @param newCharsetName
     */
    public static void convert(String oldFullFileName, String oldCharsetName, String newFullFileName, String newCharsetName) throws Exception {
        log.debug("the old file name is : {}, The oldCharsetName is : {}", oldFullFileName, oldCharsetName);
        log.debug("the new file name is : {}, The newCharsetName is : {}", newFullFileName, newCharsetName);

        Writer out;
        StringBuilder content = new StringBuilder();
        try (FileInputStream fileInputStream = new FileInputStream(oldFullFileName);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,oldCharsetName);
             BufferedReader bin = new BufferedReader(inputStreamReader);
             FileOutputStream fileOutputStream = new FileOutputStream(newFullFileName);){
            String line;
            while ((line = bin.readLine()) != null) {
                content.append(line);
                content.append(System.getProperty("line.separator"));
            }
            newFullFileName = newFullFileName.replace("\\", "/");
            File dir = new File(newFullFileName.substring(0, newFullFileName.lastIndexOf(SLASH)));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            out = new OutputStreamWriter(fileOutputStream, newCharsetName);
            out.write(content.toString());
        } catch (Exception e) {
            log.error("EncodeUtils.convert方法异常:", e);
        }
    }

    /**
     * 将一指定编码的文件转换为另一编码的文件
     *
     * @param oldStream  输入流
     * @param oldCharsetName
     * @param newCharsetName
     */
    public static InputStream convertByStream(InputStream oldStream, String oldCharsetName, String newCharsetName) {
        InputStream inputStream = null;
        try {
            String contentStr = null;
            StringBuilder content = new StringBuilder();

            @Cleanup
            BufferedReader bin = new BufferedReader(new InputStreamReader(oldStream, oldCharsetName));
            String line;
            while ((line = bin.readLine()) != null) {
                content.append(line);
                content.append(System.getProperty("line.separator"));
            }
            contentStr = content.toString();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(contentStr.getBytes(newCharsetName));
            inputStream = byteArrayInputStream;
        } catch (Exception e) {
            log.error("EncodeUtils.convert方法异常:",e);
        }
        return inputStream;
    }

    /**
     * 判断文件的编码格式
     * @param in
     * @return 文件编码格式
     * @throws Exception
     */
    public static String codeString(InputStream in) throws Exception{
        String code = null;
        try (BufferedInputStream bis = new BufferedInputStream(in)){
            int p = (bis.read() << 8) + bis.read();
            switch (p) {
                case 0xefbb:
                    code = CODE_UTF8;
                    break;
                case 0xfffe:
                    code = UNICODE;
                    break;
                case 0xfeff:
                    code = "UTF-16BE";
                    break;
                default:
                    code = CODE_GBK;
            }
        } catch (Exception e) {
            log.error("通过文件缓存流获取编码集名称方法异常:",e);
            return BLANK;
        }
        return code;
    }
}