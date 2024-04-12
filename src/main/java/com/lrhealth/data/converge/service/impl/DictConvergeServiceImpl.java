package com.lrhealth.data.converge.service.impl;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lrhealth.data.converge.common.util.file.FileToJsonUtil;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.dao.service.XdsService;
import com.lrhealth.data.converge.model.bo.ColumnDbBo;
import com.lrhealth.data.converge.model.dto.DataSourceDto;
import com.lrhealth.data.converge.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jinmengyu
 * @date 2023-10-23
 */
@Slf4j
@Service
public class DictConvergeServiceImpl implements DictConvergeService {
    @Resource
    private XdsService xdsService;
    @Resource
    private KafkaService kafkaService;
    @Resource
    private DbSqlService dbSqlService;
    @Resource
    private FileService fileService;
    @Resource
    private XdsInfoService xdsInfoService;
    @Resource
    private ConvTunnelService tunnelService;

    @Override
    public void dictConverge(MultipartFile file, String orgCode, String sysCode) {
        // 查询写入数据源
        DataSourceDto dataSourceDto = tunnelService.getDataSourceBySys(sysCode);
        // 创建字典xds
        Xds xds = xdsInfoService.createDictXds(orgCode, sysCode, file);
        // 查询是否建表
        // 获取数据，表不存在的话就行建表
        JSONObject jsonObject = new JSONObject();
        try {
            List<ReadSheet> readSheets = EasyExcelFactory.read(file.getInputStream()).build().excelExecutor().sheetList();
            for (ReadSheet sheet : readSheets) {
                List<Object> dataList = EasyExcelFactory.read(file.getInputStream()).sheet(sheet.getSheetNo()).headRowNumber(0).doReadSync();
                JSONArray dataArray = JSON.parseArray(JSON.toJSONString(dataList));
                JSONObject header = dataArray.getJSONObject(0);

                // 表不存在？ 建表
                if (!dbSqlService.checkOdsTableExist(xds.getOdsTableName(), dataSourceDto)){
                    // 创建表
                    Map<String, String> headerMap = (Map<String, String>) dataList.get(0);
                    List<ColumnDbBo> collect = headerMap.values().stream().map(s -> ColumnDbBo.builder().columnName(s).build()).collect(Collectors.toList());
                    dbSqlService.createTable(collect, xds.getOdsTableName(), dataSourceDto);
                }

                FileToJsonUtil.putSheetData(jsonObject, dataArray, header, sheet.getSheetName());
            }
        } catch (Exception e) {
            log.error("[EXCEL] {}解析异常:{}", file.getOriginalFilename(), e.getMessage());
        }
        // 数据落库
        Long count = fileService.jsonDataSave(jsonObject, xds);
        // 更新xds
        xdsService.updateById(Xds.builder().id(xds.getId())
                .dataCount(count).dataConvergeEndTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now()).build());
        // kafka发治理
        kafkaService.xdsSendKafka(xds);
    }

}
