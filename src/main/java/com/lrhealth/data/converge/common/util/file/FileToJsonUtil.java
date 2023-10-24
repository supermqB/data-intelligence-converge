package com.lrhealth.data.converge.common.util.file;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.lrhealth.data.common.exception.CommonException;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
public class FileToJsonUtil {

    private FileToJsonUtil() {
    }

    /**
     * Json输入流，输出json
     *
     * @param file 待处理文件
     * @return JSONObject
     */
    public static JSONObject jsonToJson(ProcessedFile file) {
        JSONObject result = new JSONObject();
        InputStream inputStream = file.getInputStream();
        try {
            result = JSON.parseObject(IOUtils.toString(inputStream).trim(), Feature.OrderedField);
        } catch (Exception e) {
            log.error("[JSON] {}解析异常:{}", file.getFileName(), e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("json文件流关闭异常:", e);
                }
            }
        }
        return result;
    }

    /**
     * excel输入流，输出json
     *
     * @param file 待处理文件
     * @return JSONObject对象
     */
    public static JSONObject excelToJson(ProcessedFile file) {
        JSONObject jsonObject = new JSONObject();
        InputStream stream = file.getInputStream();
        try {
            List<ReadSheet> readSheets = EasyExcelFactory.read(stream).build().excelExecutor().sheetList();
            for (ReadSheet sheet : readSheets) {
                List<Object> dataList = EasyExcelFactory.read(stream).sheet(sheet.getSheetNo()).headRowNumber(0).doReadSync();
                JSONArray dataArray = JSON.parseArray(JSON.toJSONString(dataList));
                JSONObject header = dataArray.getJSONObject(0);
                putSheetData(jsonObject, dataArray, header, sheet.getSheetName());
            }
        } catch (Exception e) {
            log.error("[EXCEL] {}解析异常:{}", file.getFileName(), e.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    log.error("close resource error,{}", ExceptionUtils.getStackTrace(e));
                }
            }
        }
        return jsonObject;
    }

    /**
     * csv输入流，输出json
     *
     *@param file 待处理文件
     *@return JSONObject对象
     */
    public static JSONObject csvToJson(ProcessedFile file) {
        String fileName = file.getFileName();
        InputStream stream = file.getInputStream();
        if (CharSequenceUtil.isBlank(file.getFileName())) {
            throw new CommonException("file name is null");
        }
        JSONArray jsonArray = null;
        try {
            jsonArray = convertToJson(stream);
        } catch (Exception e) {
            log.error("{}csv文件转换异常:", fileName, e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    log.error("{}csv文件流关闭异常:", fileName, e);
                }
            }
        }
        JSONObject jsonObject = new JSONObject();
        //表名使用截取的文件名
        String odsFileName;
        if (fileName.contains("__")) {
            odsFileName = fileName.substring(0, fileName.indexOf("__"));
        } else {
            if (fileName.contains(".")) {
                odsFileName = fileName.substring(0, fileName.indexOf("."));
            } else {
                odsFileName = fileName;
            }
        }
        //ods表名统一大写
        jsonObject.put(odsFileName.toUpperCase(), jsonArray);

        return jsonObject;
    }


    public static void putSheetData(JSONObject jsonObject, JSONArray dataArray, JSONObject header, String sheetName){
        JSONArray result = new JSONArray();
        // 数据从第1行开始，0是表头
        for (int i = 1; i < dataArray.size(); i++) {
            JSONObject temp = dataArray.getJSONObject(i);
            JSONObject index = new JSONObject(new LinkedHashMap<>());
            for (int j = 0; j < header.size(); j++) {
                if (ObjectUtil.isNotNull(header.get(String.valueOf(j)))) {
                    index.put(ObjectUtil.isEmpty(header.get(String.valueOf(j))) ? "" : (String) header.get(String.valueOf(j)),
                            ObjectUtil.isEmpty(temp.get(String.valueOf(j))) ? "" : temp.get(String.valueOf(j)));
                }
            }
            if (!index.isEmpty() && CharSequenceUtil.isNotBlank(header.get(0).toString())) {
                result.add(index);
            }
        }
        jsonObject.put(sheetName, result);
    }

    /**
     * inputStream转换为jsonArray
     * @param filePath csv文件输入流
     * @return JSONArray csv数据
     */
    private static JSONArray convertToJson(InputStream filePath) {
        InputStream streamCache=null;
        InputStreamReader isr=null;
        JSONArray jsonArray = new JSONArray();
        try {
            InputStreamCacher cacher = new InputStreamCacher(filePath);
            streamCache = cacher.getInputStream();
            String encodeType = EncodeUtils.getEncode(streamCache, true);
            streamCache = cacher.getInputStream();
            isr = new InputStreamReader(streamCache, encodeType);
            BufferedReader reader = new BufferedReader(isr);
            List<String> header;
            //将csv表格第一行构建成string
            String headerStr = reader.readLine();
            if (headerStr.trim().isEmpty()) {
                log.error("csv文件表格头不能为空");
                throw new CommonException("csv表格头不能为空");
            }
            //将String字符串通过split（","）csv是以，作分隔符
            // 进行切割输出成List
            header = stringToList(headerStr);
            String line;
            int lineCnt = 0;
            while ((line = reader.readLine()) != null) {
                lineCnt++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                List<String> lineData = stringToList(line);
                if (lineData.size() != header.size()) {
                    log.error("第{}行数据列和表头列个数不一致{}{}", lineCnt, line, lineData);
                    continue;
                }

                String jsonStr = stringToJson(header, lineData);
                //逗号去掉
                jsonStr = jsonStr.replaceAll(".$", "");

                jsonArray.add(stringToJsonObjKeepSequence(jsonStr));
            }
        }catch (Exception e){
            log.error("csv数据转换异常：{}", ExceptionUtils.getStackTrace(e));
        }finally {
            if(streamCache!=null) {
                try {
                    streamCache.close();
                } catch (IOException e) {
                    log.error("close resource error,{}", ExceptionUtils.getStackTrace(e));
                }
            }
            if(isr !=null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    log.error("close resource error,{}", ExceptionUtils.getStackTrace(e));
                }
            }
        }
        return jsonArray;
    }

    private static List<String> stringToList(String s) {
        if (s == null) {
            return Lists.newArrayList();
        }
        //正则：避免切割英文双引号内的英文逗号，-1代表结尾为空的字段不舍弃
        String[] parts = s.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
        return Arrays.asList(parts);
    }

    /**
     * 将分割的第一行的表头list和后面的值list进行拼接,拼接完后以string返回
     * @param header 表头
     * @param lineData csv一行数据
     */
    private static String stringToJson(List<String> header, List<String> lineData) {

        if (header == null || lineData == null) {
            throw new CommonException("输入不能为null");
        } else if (header.size() != lineData.size()) {
            throw new CommonException("表头个数和数据列个数不等");
        }
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("{ ");
        for (int i = 0; i < header.size(); i++) {
            //存在值自带双引号的情况
            header.set(i, header.get(i).replace("\"", ""));
            lineData.set(i, lineData.get(i).replace("\"", ""));
            String mString = String.format("\"%s\": \"%s\"", header.get(i), lineData.get(i));
            sBuilder.append(mString);
            if (i != header.size() - 1) {
                sBuilder.append(", ");
            }
        }
        sBuilder.append(" },");
        return sBuilder.toString();
    }

    /**
     * String转JSON对象，并保持key-value的顺序
     *
     * @param strJson JSON字符创
     *
     * @return JSON对象
     */
    private static JSONObject stringToJsonObjKeepSequence(String strJson) {
        LinkedHashMap<String, Object> json = JSON.parseObject(strJson, LinkedHashMap.class, Feature.OrderedField);
        JSONObject jsonObject = new JSONObject(true);
        jsonObject.putAll(json);
        return jsonObject;
    }

}


