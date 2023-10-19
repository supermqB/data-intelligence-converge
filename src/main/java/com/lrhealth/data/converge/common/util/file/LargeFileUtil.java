package com.lrhealth.data.converge.common.util.file;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.dao.adpter.BeeBaseRepository;
import com.lrhealth.data.converge.scheduled.thread.AsyncFactory;
import com.opencsv.CSVReader;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.teasoft.honey.osql.core.BeeFactory;

import javax.annotation.Resource;
import java.io.FileReader;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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


    public Integer fileParseAndSave(String filePath, Long xdsId, String odsTableName, Map<String, String> fieldTypeMap, Integer taskId) {
        int lineCnt = 0;
        PreparedStatement pst = null;
        BeeFactory instance = BeeFactory.getInstance();

        try (CSVReader reader = new CSVReader(new FileReader(filePath));
             Connection connection = instance.getDataSource().getConnection()) {
            // csv表头
            String[] headerList = reader.readNext();
            // 过滤出来的原始字段对应的csv表头索引
            Map<String, DataFieldInfo> odsHeaderMap = getFieldInfoMap(headerList, fieldTypeMap);

            pst = connection.prepareStatement(assembleSql(odsHeaderMap, odsTableName));

            String[] dataLine;
            Map<String, String> setErrorMap = new HashMap<>();
            while ((dataLine = reader.readNext()) != null) {
                boolean setFlag = true;
                Integer statementIndex = 1;
                // 设置字段的类型
                for (Map.Entry<String, DataFieldInfo> odsHeader: odsHeaderMap.entrySet()){
                    DataFieldInfo dataFieldInfo = odsHeader.getValue();
                    String value = dataLine[odsHeader.getValue().getFieldIndex()];
                    setFlag = parameterSet(dataFieldInfo, pst, statementIndex, value);
                    if ((!setFlag) && (!setErrorMap.containsKey(dataFieldInfo.getFieldName()))){
                        String errorMessage = "prepareStatement type set error: "
                                + "table:[" + odsTableName
                                + "], field:[" + dataFieldInfo.getFieldName()
                                + "], type:[" + dataFieldInfo.getFieldType()
                                + "], value:[" + value + "]";
                        setErrorMap.put(dataFieldInfo.getFieldName(), errorMessage);
                    }
                    statementIndex++;
                }
                if (setFlag){
                    // 给xds_id赋值
                    pst.setLong(statementIndex, xdsId);
                    pst.addBatch();
                    lineCnt++;
                }

                if (lineCnt % BATCH_SIZE == 0) {
                    // 达到每批数据的大小，进行入库操作
                    pst.executeBatch();
                }
                if (lineCnt % (50000) == 0){
                    AsyncFactory.convTaskLog(taskId, "[" + odsTableName + "]表已写入[" + lineCnt + "]条");
                }
            }
            pst.executeBatch();
            setErrorMap.forEach((key, value) -> AsyncFactory.convTaskLog(taskId, value));

        }catch (Exception e){
            log.error("csv数据入库异常：{}", ExceptionUtils.getStackTrace(e));
            AsyncFactory.convTaskLog(taskId, ExceptionUtils.getStackTrace(e));
            throw new CommonException("数据异常, 错误信息:{}", ExceptionUtils.getStackTrace(e));
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

    @Data
    static class DataFieldInfo{
        private String fieldName;

        private Integer fieldIndex;

        private String fieldType;
    }

    private Map<String, DataFieldInfo> getFieldInfoMap(String[] headerList, Map<String, String> fieldTypeMap){
        Map<String, DataFieldInfo> odsHeaderMap = new HashMap<>();
        for (int i = 0;i < headerList.length;i++){
            if (!fieldTypeMap.containsKey(headerList[i])){
                continue;
            }
            DataFieldInfo dataFieldInfo = new DataFieldInfo();
            dataFieldInfo.setFieldName(headerList[i]);
            dataFieldInfo.setFieldIndex(i);
            dataFieldInfo.setFieldType(fieldTypeMap.get(headerList[i]));
            odsHeaderMap.put(headerList[i], dataFieldInfo);
        }
        return odsHeaderMap;
    }


    private boolean parameterSet(DataFieldInfo dataFieldInfo, PreparedStatement stmt, Integer index, String value) {
        try {
            if (value.equals("null") || value.equals("")){
                stmt.setString(index, null);
                return true;
            }
            String fieldType = dataFieldInfo.getFieldType();
            if (CharSequenceUtil.isNotBlank(fieldType)){
                switch (fieldType) {
                    case "char":
                    case "varchar":
                    case "text":
                        stmt.setString(index, value);
                        break;
                    case "int":
                        stmt.setInt(index, Integer.parseInt(value));
                        break;
                    case "bigint":
                        stmt.setLong(index, Long.parseLong(value));
                        break;
                    case "double":
                        stmt.setDouble(index, Double.parseDouble(value));
                        break;
                    case "boolean":
                        stmt.setBoolean(index, Boolean.getBoolean(value));
                        break;
                    case "date":
                        stmt.setDate(index, Date.valueOf(value));
                        break;
                    case "timestamp":
                        stmt.setTimestamp(index, Timestamp.valueOf(value));
                        break;
                    case "time":
                        stmt.setTime(index, Time.valueOf(value));
                        break;
                    case "decimal":
                    case "bigDecimal":
                        stmt.setBigDecimal(index, new BigDecimal(value));
                        break;
                    default:
                        log.error("不存在的数据类型: {}, 字段名称: {}",fieldType, dataFieldInfo.getFieldName());
                        return false;
                }
                return true;
            }
        }catch (Exception e){
            log.error("type set error: {}", ExceptionUtils.getMessage(e));
        }
        return false;
    }


    private String assembleSql(Map<String, DataFieldInfo> odsHeaderMap, String odsTableName){
        StringBuilder fieldSql = new StringBuilder();
        StringBuilder valueSql = new StringBuilder();
        for (String str : odsHeaderMap.keySet()) {
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
