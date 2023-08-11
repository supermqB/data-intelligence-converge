package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.util.file.FileToJsonUtil;
import com.lrhealth.data.converge.dao.adpter.BeeBaseRepository;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.dao.service.XdsService;
import com.lrhealth.data.converge.model.ConvFileInfoDto;
import com.lrhealth.data.converge.service.DocumentParseService;
import com.lrhealth.data.converge.service.XdsInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.hutool.core.text.StrPool.UNDERLINE;

/**
 * <p>
 * 文档解析接口实现类
 * 文档解析支持JSON和Excel解析
 * JSON:通过系统生成的统一文件格式
 * Excel:线下或者文件上传的文档
 * </p>
 *
 * @author lr
 * @since 2023/7/19 11:47
 */
@Service
@Slf4j
public class DocumentParseServiceImpl implements DocumentParseService {

    @Resource
    private XdsService xdsService;

    @Resource
    private BeeBaseRepository beeBaseRepository;

    @Resource
    private XdsInfoService xdsInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Xds fileParseAndSave(Long id) {
        Xds xds = xdsService.getById(id);
        checkParam(xds);
        JSONObject parseData = parseFileByFilePath(xds);
        List<String> odsTableNames = new ArrayList<>(parseData.keySet());
        if (odsTableNames.size() != 1){
            // 目前excel只能使用一个sheet页，生成一条xds
            log.error("odsTable size is not allowed, data: {}", odsTableNames);
            throw new CommonException("原始文件不符合解析要求");
        }
        String odsTableName = odsTableNames.get(0);
        xds.setOdsTableName(xds.getSysCode() + UNDERLINE + odsTableName);
        xds.setOdsModelName(odsTableName.toUpperCase());
        Integer dataCount = jsonDataSave(parseData, xds);
        return xdsInfoService.updateXdsCompleted(setConvFileInfoDto(xds, dataCount));
    }

    @Override
    public Xds flinkFileParseAndSave(Xds xds) {
        JSONObject parseData = parseFlinkFile(xds);
        Integer dataCount = jsonDataSave(parseData, xds);
        return xdsInfoService.updateXdsCompleted(setConvFileInfoDto(xds, dataCount));
    }

    @Override
    public JSONObject parseFileByFilePath(Xds xds) {
        JSONObject result = new JSONObject();
        InputStream fileStream = null;
        try {
            fileStream = Files.newInputStream(Paths.get(xds.getStoredFilePath() + "/" + xds.getStoredFileName()));
            result = fileToJson(fileStream, xds.getStoredFileType(), xds.getStoredFileName());
        }catch (Exception e){
            e.getStackTrace();
        }finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    log.error("excel文件流关闭异常:", e);
                }
            }
        }
        return result;
    }

    private Integer jsonDataSave(JSONObject jsonObject, Xds xds){
        Set<String> odsTableNames = jsonObject.keySet();
        int countNumber = 0;
        for (String odsTableName : odsTableNames) {
            try {
                List<Map<String, Object>> odsDataList = (List<Map<String, Object>>) jsonObject.get(odsTableName);
                odsDataList.forEach(map -> map.put("xds_id", xds.getId()));
                beeBaseRepository.insertBatch(xds.getOdsTableName(), odsDataList);
                countNumber += odsDataList.size();
            }catch (Exception e) {
                log.error("file data to db sql exception,{}", ExceptionUtils.getStackTrace(e));
                throw new CommonException("数据插入异常, xdsId: {}", String.valueOf(xds.getId()));
            }
        }
        return countNumber;

    }

    private JSONObject fileToJson(InputStream in, String fileType, String fileName) {
        try {
            //根据文件类型处理流，输出json对象，json结构待定
            JSONObject odsAllJsonData = new JSONObject();

            switch (fileType) {
                case "json":
                    odsAllJsonData = FileToJsonUtil.jsonToJson(in);
                    break;
                case "xls":
                case "xlsx":
                    odsAllJsonData = FileToJsonUtil.excelToJsonByali(in, fileName);
                    break;
                default:
            }
            if (ObjectUtil.isNull(odsAllJsonData)) {
                throw new CommonException("文件解析为空");
            }
            return odsAllJsonData;
        } catch (Exception e) {
            log.error("文件解析异常:", e);
            throw new CommonException("文件解析异常:{}", ExceptionUtil.getMessage(e));
        }
    }

    private void checkParam(Xds xds){
        if (CharSequenceUtil.isBlank(xds.getStoredFilePath()) || CharSequenceUtil.isBlank(xds.getStoredFileType())
        || CharSequenceUtil.isBlank(xds.getStoredFileName())){
            log.error("文档解析必须字段缺失，filePath:{} fileType:{} fileName:{}", xds.getStoredFilePath(),
                     xds.getStoredFileType(), xds.getStoredFileName());
        }
        if (CharSequenceUtil.isBlank(xds.getOrgCode())){
            log.error("机构编码缺失");
        }
    }

    private ConvFileInfoDto setConvFileInfoDto(Xds xds, Integer dataCount){
        ConvFileInfoDto convFileInfoDto = new ConvFileInfoDto();
        convFileInfoDto.setId(xds.getId());
        convFileInfoDto.setDataCount(String.valueOf(dataCount));
        convFileInfoDto.setOdsModelName(xds.getOdsModelName());
        convFileInfoDto.setOdsTableName(xds.getOdsTableName());
        return convFileInfoDto;
    }


    private JSONObject parseFlinkFile(Xds xds){
        JSONObject result = new JSONObject();
        BufferedReader reader = null;
        try {
            StringBuilder content = new StringBuilder();
            reader = new BufferedReader(new FileReader(xds.getStoredFilePath() + "/" + xds.getStoredFileName()));
            String line;
            JSONArray jsonArray = new JSONArray();
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            String[] objectString = content.toString().split("}(?=\\{)");
            for (int i = 0; i < objectString.length; i++){
                if (i != objectString.length - 1){
                    objectString[i] += "}";
                }
                JSONObject jsonObject = JSON.parseObject(objectString[i]);
                jsonArray.add(jsonObject);
            }
            result.put(xds.getOdsModelName(), jsonArray);
        }catch (Exception e){
            log.error("flink文件读取异常: {}", ExceptionUtils.getStackTrace(e));
            throw new CommonException("文件读取异常: {}", ExceptionUtils.getStackTrace(e));
        }finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("excel文件流关闭异常:", e);
                }
            }
        }
        return result;
    }
}
