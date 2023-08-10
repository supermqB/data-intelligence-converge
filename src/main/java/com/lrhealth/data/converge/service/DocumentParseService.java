package com.lrhealth.data.converge.service;

import com.alibaba.fastjson.JSONObject;
import com.lrhealth.data.converge.dao.entity.Xds;

/**
 * <p>
 * 文档解析接口
 * </p>
 *
 * @author lr
 * @since 2023/7/19 11:47
 */
public interface DocumentParseService {

    /**
     * 解析文件，然后落库
     * @param id 已经建立的xds信息id
     */
    Xds fileParseAndSave(Long id);

    /**
     * 解析文件
     * 目前支持的文件解析类型： json/excel
     * @param xds 已经建立的xds信息
     * @return JSONObject 返回json类型数据
     */
    JSONObject parseFileByFilePath(Xds xds);

    Xds flinkFileParseAndSave(Xds xds);
}
