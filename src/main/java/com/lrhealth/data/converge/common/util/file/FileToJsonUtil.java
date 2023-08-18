package com.lrhealth.data.converge.common.util.file;


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
                putSheetData(jsonObject, sheet, stream);
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

    private static void putSheetData(JSONObject jsonObject, ReadSheet sheet, InputStream stream){
        try {
            List<Object> dataList = EasyExcelFactory.read(stream).sheet(sheet.getSheetNo()).headRowNumber(0).doReadSync();
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
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    log.error("close resource error,{}", ExceptionUtils.getStackTrace(e));
                }
            }
        }
    }


}


