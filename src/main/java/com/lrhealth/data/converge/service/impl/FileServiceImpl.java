package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.util.file.FileToJsonUtil;
import com.lrhealth.data.converge.common.util.file.ProcessedFile;
import com.lrhealth.data.converge.dao.adpter.BeeBaseRepository;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.dto.ConvFileInfoDto;
import com.lrhealth.data.converge.model.dto.FileConvergeInfoDTO;
import com.lrhealth.data.converge.service.FileService;
import com.lrhealth.data.converge.service.ShellService;
import com.lrhealth.data.converge.service.XdsInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.hutool.core.text.CharSequenceUtil.isBlank;
import static cn.hutool.core.text.StrPool.*;

/**
 * @author jinmengyu
 * @date 2023-07-27
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {
    @Resource
    private BeeBaseRepository beeBaseRepository;
    @Resource
    private ShellService shellService;
    @Resource
    private XdsInfoService xdsInfoService;

    @Override
    public void uploadFile(MultipartFile file, String projectId) {
        /*String oriFilePath = getConfig(projectId);
        String filename = file.getOriginalFilename();
        Path targetUrl = Paths.get( oriFilePath + SLASH + filename);*/
        Path targetUrl = Paths.get(projectId);
        log.info("源文件名称: {} ---> 上传文件目录: {}", file.getName(), targetUrl);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetUrl, StandardCopyOption.REPLACE_EXISTING);
        }catch (Exception e){
            log.error(ExceptionUtil.stacktraceToString(e));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Xds fileConverge(FileConvergeInfoDTO fileConfig, Long xdsId) {
        Xds xds = xdsInfoService.getById(xdsId);
        // 文件搬运，组装文件参数信息
        ConvFileInfoDto convFileInfoDto = fileCopy(fileConfig, xds);
        Xds updatedFileXds = xdsInfoService.updateXdsFileInfo(convFileInfoDto);
        // 文件解析,数据落库
        Long dataCount = fileParseAndSave(updatedFileXds);
        return xdsInfoService.updateXdsCompleted(setConvFileInfoDto(xds, dataCount));
    }

    @Override
    public Xds flinkFileConverge(FileConvergeInfoDTO fileConfig, Long xdsId) {
        Xds xds = xdsInfoService.getById(xdsId);
        // 文件搬运，组装文件参数信息
        ConvFileInfoDto convFileInfoDto = fileCopy(fileConfig, xds);
        Xds updatedFileXds = xdsInfoService.updateXdsFileInfo(convFileInfoDto);
        // flink采集的json文件处理
        Long dataCount = flinkFileParseAndSave(updatedFileXds);
        return xdsInfoService.updateXdsCompleted(setConvFileInfoDto(xds, dataCount));
    }

    private ConvFileInfoDto fileCopy(FileConvergeInfoDTO fileConfig, Xds xds){
        fileConfig.setStoredFilePath(fileConfig.getStoredFilePath() + SLASH + xds.getOrgCode() + SLASH +  xds.getSysCode());
        // shell进行搬运
        String storedFileName = shellService.execShell(fileConfig, xds.getId());
        // 文件已经到文件解析下的目录,可以统一处理
        return ConvFileInfoDto.builder().id(xds.getId())
                .oriFileName(fileConfig.getOriFileName()).oriFileFromIp(fileConfig.getFrontendIp())
                .oriFileType(fileConfig.getOriFileType()).oriFileSize(fileConfig.getOriFileSize())
                .storedFileName(storedFileName).storedFileType(fileConfig.getOriFileType())
                .storedFilePath(fileConfig.getStoredFilePath()).build();
    }

    private Long fileParseAndSave(Xds xds) {
        checkParam(xds);
        JSONObject parseData = parseFileByFilePath(xds);
        if (isBlank(xds.getOdsTableName()) || isBlank(xds.getOdsModelName())){
            List<String> odsTableNames = new ArrayList<>(parseData.keySet());
            if (odsTableNames.size() != 1){
                // 目前excel只能使用一个sheet页，生成一条xds
                log.error("odsTable size is not allowed, data: {}", odsTableNames);
                throw new CommonException("原始文件不符合解析要求");
            }
            String odsTableName = odsTableNames.get(0);
            xds.setOdsTableName(xds.getSysCode() + UNDERLINE + odsTableName);
            xds.setOdsModelName(odsTableName.toUpperCase());
        }
        return jsonDataSave(parseData, xds);
    }

    private Long flinkFileParseAndSave(Xds xds) {
        JSONObject parseData = parseFlinkFile(xds);
        return jsonDataSave(parseData, xds);
    }

    private ConvFileInfoDto setConvFileInfoDto(Xds xds, Long dataCount){
        ConvFileInfoDto convFileInfoDto = new ConvFileInfoDto();
        convFileInfoDto.setId(xds.getId());
        convFileInfoDto.setDataCount(String.valueOf(dataCount));
        convFileInfoDto.setOdsModelName(xds.getOdsModelName());
        convFileInfoDto.setOdsTableName(xds.getOdsTableName());
        return convFileInfoDto;
    }

    private void checkParam(Xds xds){
        String filePath = xds.getStoredFilePath();
        String fileType = xds.getStoredFileType();
        String fileName = xds.getStoredFileName();
        String orgCode = xds.getOrgCode();
        if (isBlank(filePath) || isBlank(fileType)
                || isBlank(fileName)){
            log.error("文档解析必需字段缺失，filePath:{} fileType:{} fileName:{}", filePath,
                    fileType, fileName);
        }
        if (isBlank(orgCode)){
            log.error("机构编码缺失");
        }
    }

    public JSONObject parseFileByFilePath(Xds xds) {
        JSONObject result = new JSONObject();
        InputStream fileStream = null;
        try {
            String fileName = xds.getStoredFileName();
            String fileType = xds.getStoredFileType();
            fileStream = Files.newInputStream(Paths.get(xds.getStoredFilePath() + SLASH + fileName));
            result = fileToJson(new ProcessedFile(fileStream, fileName, fileType));
        } catch (Exception e){
            e.getStackTrace();
        } finally {
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

    private JSONObject fileToJson(ProcessedFile file) {
        //根据文件类型处理流，输出json对象，json结构待定
        JSONObject odsAllJsonData = new JSONObject();
        switch (file.getFileType()) {
            case "json":
                odsAllJsonData = FileToJsonUtil.jsonToJson(file);
                break;
            case "csv":
                odsAllJsonData = FileToJsonUtil.csvToJson(file);
                break;
            case "xls":
            case "xlsx":
                odsAllJsonData = FileToJsonUtil.excelToJson(file);
                break;
            default:
        }
        if (ObjectUtil.isNull(odsAllJsonData)) {
            throw new CommonException("文件解析为空");
        }
        return odsAllJsonData;
    }

    @Override
    public Long jsonDataSave(JSONObject jsonObject, Xds xds){
        Set<String> odsTableNames = jsonObject.keySet();
        long countNumber = 0;
        for (String odsTableName : odsTableNames) {
            try {
                List<Map<String, Object>> odsDataList = (List<Map<String, Object>>) jsonObject.get(odsTableName);
                odsDataList.forEach(map -> map.put("xds_id", xds.getId()));
                beeBaseRepository.insertBatch(xds.getSysCode(), xds.getOdsTableName(), odsDataList);
                countNumber += odsDataList.size();
            }catch (Exception e) {
                log.error("file data to db sql exception,{}", ExceptionUtils.getStackTrace(e));
                throw new CommonException("数据插入异常, xdsId: {}", String.valueOf(xds.getId()));
            }
        }
        return countNumber;
    }


    private JSONObject parseFlinkFile(Xds xds){
        JSONObject result = new JSONObject();
        BufferedReader reader = null;
        try {
            StringBuilder content = new StringBuilder();
            reader = new BufferedReader(new FileReader(xds.getStoredFilePath() + SLASH + xds.getStoredFileName()));
            String line;
            JSONArray jsonArray = new JSONArray();
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            String[] objectString = content.toString().split("}(?=\\{)");
            for (int i = 0; i < objectString.length; i++){
                if (i != objectString.length - 1){
                    objectString[i] += DELIM_END;
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
