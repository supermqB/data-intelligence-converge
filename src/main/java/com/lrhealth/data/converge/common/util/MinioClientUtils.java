package com.lrhealth.data.converge.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.lrhealth.data.converge.common.config.MinioConfig;
import com.lrhealth.data.converge.common.exception.CommonException;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * 对象存储MinIO客户端：管理桶、管理文件对象
 * @author jinmengyu
 * @date 2024-03-27
 */
@Slf4j
@Component
public class MinioClientUtils {
    @Resource
    private MinioConfig prop;
    @Resource
    private MinioClient minioClient;

    /**
     * 文件上传
     *
     * @param file 文件
     * @return Boolean
     */
    public String upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (CharSequenceUtil.isBlank(originalFilename)){
            throw new CommonException("文件名为空");
        }
        String fileName = IdUtil.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = DateUtil.now() + "/" + fileName;
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(prop.getBucketName()).object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build();
            //文件名称相同会覆盖
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return objectName;
    }


    /**
     * 文件下载
     * @param fileName 文件名称
     */
    public boolean download(String objectStroagePath, String fileName, String fileStoragePath) {
        GetObjectArgs.Builder builder = GetObjectArgs.builder();
        builder.bucket(prop.getBucketName());
        builder.object(objectStroagePath);

        GetObjectArgs objectArgs = builder.build();
        try {
            GetObjectResponse response = minioClient.getObject(objectArgs);

            // Read the byte stream of data
            byte[] buff = new byte[1024];
            FastByteArrayOutputStream fbaos = new FastByteArrayOutputStream();
            int len = 0;
            while ((len = response.read(buff)) != -1) {
                fbaos.write(buff, 0, len);
            }
            fbaos.flush();

            // Create the file
            File file = new File(fileStoragePath + fileName);
            if (!file.exists()) {
                String path = file.getParent();
                File pathFile = new File(path);
                if (!pathFile.exists()){
                    pathFile.mkdirs();
                }
                file.createNewFile();
            }

            // Write the file
            OutputStream outputStream = new FileOutputStream(file);
            outputStream.write(fbaos.toByteArray());
            outputStream.flush();
            fbaos.close();

        }catch (Exception e){
            log.error("文件[{}]下载失败: {}", fileName, ExceptionUtils.getStackTrace(e));
        }
        return true;

    }
}
