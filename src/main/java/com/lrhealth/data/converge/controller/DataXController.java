package com.lrhealth.data.converge.controller;

import com.lrhealth.data.common.result.ResultBase;
import com.lrhealth.data.converge.common.util.file.LargeFileUtil;
import com.lrhealth.data.converge.service.DataXExecService;
import com.lrhealth.data.converge.service.OdsModelService;
import com.lrhealth.data.model.original.model.OriginalModelColumn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jinmengyu
 * @date 2023-09-01
 */
@Slf4j
@RestController()
@RequestMapping("/datax")
public class DataXController {
    @Resource
    private DataXExecService dataXService;


    @PostMapping("/exec")
    public ResultBase execJson(){
        try {
            String jsonPath = "D:\\Users\\admin\\Desktop\\datax\\job\\admission_record.json";
            dataXService.dataXExec(jsonPath, 265L, "Standalone", 1000);
            return ResultBase.success();
        } catch (Exception e) {
            return ResultBase.fail(e.getMessage());
        }

    }

    @Resource
    private OdsModelService odsModelService;
    @Resource
    private LargeFileUtil largeFileUtil;

    @PostMapping("/file")
    public void fileSaveTest(){
        List<OriginalModelColumn> originalModelColumns = odsModelService.getcolumnList("patient", "S5200000034001");

        List<OriginalModelColumn> filterModelColumns = new ArrayList<>(originalModelColumns.stream().collect(Collectors.toMap(OriginalModelColumn::getNameEn, column -> column, (column1, column2) -> column1))
                .values());
        Map<String, String> fieldTypeMap = filterModelColumns.stream().collect(Collectors.toMap(OriginalModelColumn::getNameEn, OriginalModelColumn::getFieldType));
        Integer countNumber = largeFileUtil.fileParseAndSave("D:\\java project\\file\\patient___db26870d_6a4d_42c0_bcba_b2d362bacc2d", 12222332L, "S5200000034001_patient", fieldTypeMap, 12211);

    }
}
