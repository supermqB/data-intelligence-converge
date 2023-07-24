package com.lrhealth.data.converge.util;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
public class FileToJsonUtil {

    private FileToJsonUtil() {
    }

    /**
     * Json输入流，输出json
     *
     * @param inputStream 文件输入流
     * @return JSONObject
     */
    public static JSONObject jsonToJson(InputStream inputStream) {

        JSONObject result;
        try {
            result = JSON.parseObject(IOUtils.toString(inputStream).trim(), Feature.OrderedField);
        } catch (Exception e) {
            log.error("转换json文件错误:", e);
            return new JSONObject();
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
     * @param inputStream excel输入流
     * @return JSONObject对象
     */
    public static JSONObject excelToJsonByali(InputStream inputStream, String filename) {
        JSONObject jsonObject;
        try {
            jsonObject = easyExcelToJson(inputStream);
        } catch (Exception e) {
            log.error("{}excel文件转换异常:", filename, e);
            return new JSONObject();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("excel文件流关闭异常:", e);
                }
            }
        }
        return jsonObject;
    }

    public static JSONObject easyExcelToJson(InputStream inputStream) {
        InputStreamCacher streamCacher = new InputStreamCacher(inputStream);
        InputStream streamCache = streamCacher.getInputStream();
        try {
            JSONObject jsonObject = new JSONObject();
            List<ReadSheet> readSheets = EasyExcelFactory.read(streamCache).build().excelExecutor().sheetList();
            for (ReadSheet sheet : readSheets) {
                putSheetData(jsonObject, sheet, streamCacher);
            }
            return jsonObject;
        } finally {
            if (streamCache != null) {
                try {
                    streamCache.close();
                } catch (IOException e) {
                    log.error("close resource error,{}", ExceptionUtils.getStackTrace(e));
                }
            }
        }
    }

    private static void putSheetData(JSONObject jsonObject, ReadSheet sheet, InputStreamCacher streamCacher){
        InputStream streamCache1 = null;
        try {
            streamCache1 = streamCacher.getInputStream();
            List<Object> dataList = EasyExcelFactory.read(streamCache1).sheet(sheet.getSheetNo()).headRowNumber(0).doReadSync();
            JSONArray dataArray = JSON.parseArray(JSON.toJSONString(dataList));
            JSONObject header = dataArray.getJSONObject(0);
            JSONArray result = new JSONArray();
            for (int i = 1; i < dataArray.size(); i++) {
                JSONObject temp = dataArray.getJSONObject(i);
                JSONObject index = new JSONObject(new LinkedHashMap<>());
                for (int j = 0; j < header.size(); j++) {
                    if (ObjectUtil.isNotNull(header.get(String.valueOf(j)))) {
                        index.put(ObjectUtil.isEmpty(header.get(String.valueOf(j))) ? "" : (String) header.get(String.valueOf(j)),
                                ObjectUtil.isEmpty(temp.get(String.valueOf(j))) ? "" : temp.get(String.valueOf(j)));
                    }
                }
                if (!index.isEmpty() && index.get(header.get(0).toString()) != null && !index.get(header.get(0).toString()).equals("")) {
                    result.add(index);
                }
            }
            jsonObject.put(sheet.getSheetName(), result);
        }catch (Exception e){
            e.getStackTrace();
        } finally {
            if (streamCache1 != null) {
                try {
                    streamCache1.close();
                } catch (IOException e) {
                    log.error("close resource error,{}", ExceptionUtils.getStackTrace(e));
                }
            }
        }
    }
}
