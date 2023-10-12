package com.lrhealth.data.converge.common.util.file;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.dao.adpter.BeeBaseRepository;
import com.lrhealth.data.converge.model.FieldMatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.teasoft.honey.osql.core.BeeFactory;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jinmengyu
 * @date 2023-09-27
 */
@Slf4j
@Service
public class LargeFileUtil {
    @Resource
    private BeeBaseRepository beeBaseRepository;


    private LargeFileUtil(){}

    private static final int BATCH_SIZE = 10000; // 每批数据的大小

    static ConcurrentMap<String, List<String>> headerMap = new ConcurrentHashMap<>();


    private void insertBatchData(List<Map<String, Object>> odsDataList, String odsTableName){
        try {
            beeBaseRepository.insertBatch(odsTableName, odsDataList);
        }catch (Exception e) {
            log.error("file data to db sql exception,{}", ExceptionUtils.getStackTrace(e));
            throw new CommonException("数据插入异常, odsTableName: {}", odsTableName);
        }
    }

    public Integer csvParseAndInsert(String filePath, String fileName, Long xdsId, String odsTableName, Map<String, String> odsColumnTypeMap) {
        Integer countNumber = null;
        try {
//            countNumber = convertToJson(filePath, fileName, xdsId, odsTableName);
              countNumber = fileParseAndSave(filePath, xdsId, odsTableName, odsColumnTypeMap);
        } catch (Exception e) {
            log.error("{}csv文件入库异常:", fileName, e);
        }
        return countNumber;
    }

    private Integer convertToJson(String filePath, String fileName, Long xdsId, String odsTableName) {
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

    private Integer fileParseAndSave(String filePath, Long xdsId, String odsTableName, Map<String, String> odsColumnTypeMap) {
        int lineCnt = 0;
        PreparedStatement pst = null;
        Map<String, FieldMatch> fieldMatchMap = parseFieldFormat(odsColumnTypeMap);

        BeeFactory instance = BeeFactory.getInstance();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
             Connection connection = instance.getDataSource().getConnection()) {
            String line;
            String header = reader.readLine();
            String[] headerList = header.split(",");

            pst = connection.prepareStatement(assembleSql(headerList, odsTableName));

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                // 设置字段的类型
                for (int i = 0;i < data.length;i++){
                    parameterSet(fieldMatchMap.get(headerList[i]), pst, (i + 1), data[i]);
                }
                // 给xds_id赋值
                pst.setLong(data.length + 1, xdsId);
                pst.addBatch();
                lineCnt++;
                if (lineCnt % BATCH_SIZE == 0) {
                    // 达到每批数据的大小，进行入库操作
                    pst.executeBatch();
                }
                if (lineCnt % (50000) == 0){
                    log.info("{}已写入[{}]条", odsTableName, lineCnt);
                }
            }
            pst.executeBatch();
        }catch (Exception e){
            log.error("csv数据入库异常：{}", ExceptionUtils.getStackTrace(e));
        }finally {
            if(pst!=null) {
                try {
                    pst.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return lineCnt;
    }


    private void parameterSet(FieldMatch fieldMatch, PreparedStatement stmt, Integer index, String value) {
        try {
            if (fieldMatch.getFieldType() != null && !value.equals("null")){
                switch (fieldMatch.getFieldType()) {
                    case "A":
                    case "AN":
                        stmt.setString(index, value);
                        break;
                    case "N":
                        if (fieldMatch.isHasDecimalPlaces()){
                            // 有小数
                            stmt.setDouble(index, Double.parseDouble(value));
                            break;
                        }
                        if (Long.parseLong(value) > Integer.MAX_VALUE){
                            // 长度较长，按照long进行处理
                            stmt.setLong(index, Long.parseLong(value));
                            break;
                        }
                        stmt.setInt(index, Integer.parseInt(value));
                        break;
                    case "D":
                        stmt.setDate(index, Date.valueOf(value));
                        break;
                    case "DT":
                        stmt.setTimestamp(index, Timestamp.valueOf(value));
                        break;
                    case "T":
                        stmt.setTime(index, Time.valueOf(value));
                        break;
                    default:
                        log.error("不存在的数据类型, {}", fieldMatch.getFieldFormat());
                }
            }else {
                stmt.setString(index, value);
            }
        }catch (Exception e){
            log.error("type match error: {}",ExceptionUtils.getStackTrace(e));
        }
    }

    private Map<String, FieldMatch> parseFieldFormat(Map<String, String> odsColumnTypeMap){
        if (CollUtil.isEmpty(odsColumnTypeMap)){
            log.error("ods column elementType is null");
            return new HashMap<>();
        }
        Map<String, FieldMatch> fieldMatchMap = new HashMap<>();
        odsColumnTypeMap.forEach((header, fieldFormat) -> {
            String fieldType = null;
            boolean hasDecimalPlaces = false;
            Pattern pattern = Pattern.compile("^(AN|A|D|DT|T|N)((?:\\.\\.\\d+)?|\\d+)?(?:,(\\d+))?$");
            Matcher matcher = pattern.matcher(fieldFormat);
            if (matcher.find()){
                fieldType = matcher.group(1);
                hasDecimalPlaces = matcher.group(3) != null;
            }
            fieldMatchMap.put(header, FieldMatch.builder().fieldFormat(fieldFormat).fieldType(fieldType).hasDecimalPlaces(hasDecimalPlaces).build());
        });
        return fieldMatchMap;
    }


    private String assembleSql(String[] headerList, String odsTableName){
        StringBuilder fieldSql = new StringBuilder();
        StringBuilder valueSql = new StringBuilder();
        for (String str : headerList) {
            fieldSql.append(str).append(",");
            valueSql.append("?").append(",");
        }
        // 处理xds_id
        fieldSql.append("xds_id").append(",");
        valueSql.append("?").append(",");
        // 组装sql
        return  "INSERT INTO " + odsTableName + "(" +
                fieldSql.substring(0, fieldSql.toString().length() - 1) + ")" +
                " VALUES " + "(" +
                valueSql.substring(0, valueSql.toString().length() - 1) + ")";
    }



    /**
     * String转JSON对象，并保持key-value的顺序
     *
     * @param strJson JSON字符创
     *
     * @return JSON对象
     */
    private Map<String, Object> stringToJsonObjKeepSequence(String strJson) {
        LinkedHashMap<String, Object> json = JSON.parseObject(strJson, LinkedHashMap.class, Feature.OrderedField);
        Map<String, Object> jsonObject = new HashMap<>(json);
        return jsonObject;
    }

    private List<String> stringToList(String s) {
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
    private String stringToJson(List<String> header, List<String> lineData) {

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
