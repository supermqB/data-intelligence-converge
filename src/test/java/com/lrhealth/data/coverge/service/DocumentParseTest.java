package com.lrhealth.data.coverge.service;

import com.alibaba.fastjson.JSONObject;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.service.DocumentParseService;
import com.lrhealth.data.converge.service.impl.DocumentParseServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author jinmengyu
 * @date 2023-07-20
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DocumentParseServiceImpl.class)
public class DocumentParseTest {

    @Resource
    private DocumentParseService documentParseService;

    @Test
    public void ExcelParseTest(){
        Xds xds = new Xds();
        xds.setStoredFileType("xlsx");
        xds.setStoredFilePath("C:\\files/1607752.xlsx");
        xds.setStoredFileName("1607752.xlsx");
        JSONObject jsonObject = documentParseService.parseFileByFilePath(xds);
        System.out.println(jsonObject);

    }

    @Test
    public void JsonParseTest(){
        Xds xds = new Xds();
        xds.setStoredFileType("json");
        xds.setStoredFilePath("C:\\files/kc52.json");
        xds.setStoredFileName("kc52.json");
        JSONObject jsonObject = documentParseService.parseFileByFilePath(xds);
        System.out.println(jsonObject);
    }

}
