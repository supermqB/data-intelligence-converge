package com.lrhealth.data.converge.common.util.file;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;

@Slf4j
public class FileToJsonUtil {

    private FileToJsonUtil() {
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


}


