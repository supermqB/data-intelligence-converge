package com.lrhealth.data.converge.common.util.file;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.dao.adpter.BeeBaseRepository;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jinmengyu
 * @date 2023-09-27
 */
@Component
public class LargeFileUtil {
    private static Logger log = LoggerFactory.getLogger(LargeFileUtil.class);

    @Autowired
    private BeeBaseRepository beeBaseRepository;

    private static LargeFileUtil largeFileUtil;

    private LargeFileUtil(){}

    private static final int BATCH_SIZE = 50000; // 每批数据的大小

    public static ConcurrentHashMap<String, List<String>> headerMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init(){
        largeFileUtil = this;
        largeFileUtil.beeBaseRepository = this.beeBaseRepository;
    }

    private static void insertBatchData(List<Map<String, Object>> odsDataList, String odsTableName){
        try {
            largeFileUtil.beeBaseRepository.insertBatch(odsTableName, odsDataList);
        }catch (Exception e) {
            log.error("file data to db sql exception,{}", ExceptionUtils.getStackTrace(e));
            throw new CommonException("数据插入异常, odsTableName: {}", odsTableName);
        }
    }


    /**
     * csv输入流，输出json
     *
     *@return JSONObject对象
     */
    public static Integer csvParseAndInsert(String filePath, String fileName, Long xdsId, String odsTableName) {
        Integer countNumber = null;
        try {
            countNumber = convertToJson(filePath, fileName, xdsId, odsTableName);
        } catch (Exception e) {
            log.error("{}csv文件入库异常:", fileName, e);
        }
        return countNumber;
    }


    /**
     * inputStream转换为jsonArray
     * @param filePath csv文件输入流
     * @return JSONArray csv数据
     */
    private static Integer convertToJson(String filePath, String fileName, Long xdsId, String odsTableName) {
        List<Map<String, Object>> batchData = new ArrayList<>(); // 存储每批数据
        int lineCnt = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            List<String> header;
            if (ObjectUtil.isNotNull(headerMap.get(fileName))) {
                header = headerMap.get(fileName);
            } else {
                //将csv表格第一行构建成string
                String headerStr = reader.readLine();
                if (headerStr.trim().isEmpty()) {
                    log.error("csv文件表格头不能为空");
                    throw new CommonException("csv表格头不能为空");
                }
                //将String字符串通过split（","）csv是以，作分隔符
                // 进行切割输出成List
                header = stringToList(headerStr);
                headerMap.put(fileName, header);
            }
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

                Map<String, Object> stringObjectMap = stringToJsonObjKeepSequence(jsonStr);
                stringObjectMap.put("xds_id", xdsId);
                batchData.add(stringObjectMap);

                if (lineCnt % BATCH_SIZE == 0) {
                    // 达到每批数据的大小，进行入库操作
                     insertBatchData(batchData, odsTableName);
                    // 清空批数据列表，准备处理下一批数据
                    batchData.clear();
                }
            }

            // 处理剩余的数据（可能不足一批）
            if (!batchData.isEmpty()) {
                insertBatchData(batchData, odsTableName);
                batchData.clear();
            }
        }catch (Exception e){
            log.error("csv数据转换异常：{}", ExceptionUtils.getStackTrace(e));
        }
        return lineCnt;
    }


    /**
     * String转JSON对象，并保持key-value的顺序
     *
     * @param strJson JSON字符创
     *
     * @return JSON对象
     */
    private static Map<String, Object> stringToJsonObjKeepSequence(String strJson) {
        LinkedHashMap<String, Object> json = JSON.parseObject(strJson, LinkedHashMap.class, Feature.OrderedField);
        Map<String, Object> jsonObject = new HashMap<>(json);
        return jsonObject;
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

}
