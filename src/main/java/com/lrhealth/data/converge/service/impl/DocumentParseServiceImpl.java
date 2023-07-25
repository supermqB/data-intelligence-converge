package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.dao.adpter.BeeBaseRepository;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.dao.service.XdsService;
import com.lrhealth.data.converge.service.DocumentParseService;
import com.lrhealth.data.converge.util.FileToJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    @Override
    public void documentParseAndSave(Long id) {
        Xds xds = xdsService.getById(id);
        checkParam(xds);
        JSONObject parseData = parseFileByFilePath(xds);
        jsonDataSave(parseData, xds.getSysCode());
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

    public void jsonDataSave(JSONObject jsonObject, String orgCode){
        Set<String> odsTableNames = jsonObject.keySet();
        for (String odsTableName : odsTableNames) {
            try {
                String tableName = orgCode + UNDERLINE + odsTableName;
                List<Map<String, Object>> odsDataList = (List<Map<String, Object>>) jsonObject.get(odsTableName);
                String batchNo = IdUtil.randomUUID();
                odsDataList.forEach(map -> map.put("batch_no", batchNo));
                beeBaseRepository.insertBatch(tableName, odsDataList);
            }catch (Exception e) {
                log.error("file data to db sql exception,{}", ExceptionUtils.getStackTrace(e));
            }
        }

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
        if (CharSequenceUtil.isNotBlank(xds.getStoredFilePath()) || CharSequenceUtil.isNotBlank(xds.getStoredFileType())
        || CharSequenceUtil.isNotBlank(xds.getStoredFileName())){
            log.error("文档解析必须字段缺失，filePath:{} fileType:{} fileName:{}", xds.getStoredFilePath(),
                     xds.getStoredFileType(), xds.getStoredFileName());
        }
        if (CharSequenceUtil.isNotBlank(xds.getOrgCode())){
            log.error("机构编码缺失");
        }
    }
}
