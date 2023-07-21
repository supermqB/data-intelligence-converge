package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.service.DocumentParseService;
import com.lrhealth.data.converge.util.FileToJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    @Override
    public JSONObject parseFileByFilePath(Xds xds) {
        JSONObject result = new JSONObject();
        if (CharSequenceUtil.isBlank(xds.getStoredFilePath())) {
            log.error("collectFilePath is null");
        }
        try {
            InputStream fileStream = Files.newInputStream(Paths.get(xds.getStoredFilePath()));
            result = fileToJson(fileStream, xds);
        }catch (Exception e){
            e.getStackTrace();
        }
        return result;
    }

    private JSONObject fileToJson(InputStream in, Xds xds) {
        try {
            //根据文件类型处理流，输出json对象，json结构待定
            JSONObject odsAllJsonData = new JSONObject();
            String fileType = xds.getStoredFileType();

            switch (fileType) {
                case "json":
                    odsAllJsonData = FileToJsonUtil.jsonToJson(in);
                    break;
                case "xls":
                case "xlsx":
                    odsAllJsonData = FileToJsonUtil.excelToJsonByali(in, xds.getStoredFileName());
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
}
