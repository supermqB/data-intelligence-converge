package com.lrhealth.data.coverge.service;

import cn.hutool.core.codec.Base64Decoder;
import com.lrhealth.data.converge.DataConvergeApplication;
import com.lrhealth.data.converge.common.util.file.FileUtils;
import com.lrhealth.data.converge.scheduled.model.FileTask;
import com.lrhealth.data.converge.scheduled.model.dto.PreFileStatusDto;
import com.lrhealth.data.converge.scheduled.service.ConvergeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;

/**
 * @author jinmengyu
 * @since 2023-07-20
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DataConvergeApplication.class)
public class DocumentParseTest {


    @Resource
    private ConvergeService convergeService;

//    @Test
//    public void ExcelParseTest(){
//        Xds xds = new Xds();
//        xds.setStoredFileType("xlsx");
//        xds.setStoredFilePath("src/test/resources/file/1607752.xlsx");
//        xds.setStoredFileName("1607752.xlsx");
//        JSONObject jsonObject = documentParseService.parseFileByFilePath(xds);
//        System.out.println(jsonObject);
//
//    }

//    @Test
//    public void JsonParseTest(){
//        Xds xds = new Xds();
//        xds.setStoredFileType("json");
//        xds.setStoredFilePath("src/test/resources/file/kc52.json");
//        xds.setStoredFileName("kc52.json");
//        JSONObject jsonObject = documentParseService.parseFileByFilePath(xds);
//        System.out.println(jsonObject);
//    }

    @Value("${lrhealth.converge.path}")
    private String path;

    @Test
    public void downLoad() throws InterruptedException {
        String url = "127.0.0.1:18081/file";
        //n9Yub4YFPrlSX2iWH169HQ==
        FileTask fileTask = new FileTask(1,"di_dws_public_health_service_record_csv");
        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        for (int i = 1; i < 10; i++) {
            stringIntegerHashMap.put("di_dws_public_health_service_record.csv.0" + i + ".part",18045360);
        }
        stringIntegerHashMap.put("di_dws_public_health_service_record.csv.10.part",9557264);
        PreFileStatusDto preFileStatusDto = new PreFileStatusDto("1", "", "",stringIntegerHashMap );
        File file =
                new File(path + File.separator + fileTask.getTaskId()
                        + File.separator + fileTask.getFileName() + File.separator);
        if (!file.exists()){
            if (!file.mkdirs()){
                System.out.println("创建文件夹失败！");
            }
        }
        //convergeService.downLoadFile(url,file,fileTask,preFileStatusDto);
        Thread.sleep(30000);// 稍等10秒，等前面的小文件全都写完

    }
    @Test
    public void merge() throws Exception {

        FileUtils fileUtils = new FileUtils();
        FileTask frontNodeTask = new FileTask(1,"di_dws_public_health_service_record_csv");
        String destPath = path + File.separator + frontNodeTask.getTaskId()
                + File.separator + frontNodeTask.getFileName() + File.separator;
//        fileUtils.mergePartFiles(threadPoolTaskExecutor, destPath, ".part",
//                200*1024*1024, destPath + File.separator
//                        + frontNodeTask.getFileName(),
//                Base64Decoder.decode("n9Yub4YFPrlSX2iWH169HQ=="));
        Thread.sleep(30000);// 稍等10秒，等前面的小文件全都写完
    }

}
