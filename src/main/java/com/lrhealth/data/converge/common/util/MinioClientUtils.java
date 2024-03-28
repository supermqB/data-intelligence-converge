package com.lrhealth.data.converge.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
     * 查看存储bucket是否存在
     * @return boolean
     */
    public Boolean bucketExists(String bucketName) {
        boolean found;
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return found;
    }

    /**
     * 创建存储bucket
     * @return Boolean
     */
    public Boolean makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    /**
     * 删除存储bucket
     * @return Boolean
     */
    public Boolean removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    /**
     * 获取全部bucket
     */
    public List<Bucket> getAllBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }



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
     * 预览图片
     */
    public String preview(String fileName){
        // 查看文件地址
        GetPresignedObjectUrlArgs build = GetPresignedObjectUrlArgs.builder().bucket(prop.getBucketName()).object(fileName).method(Method.GET).build();
        try {
            return minioClient.getPresignedObjectUrl(build);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    /**
     * 查看文件对象
     * @return 存储bucket内文件对象信息
     */
    public List<Item> listObjects() {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(prop.getBucketName()).build());
        List<Item> items = new ArrayList<>();
        try {
            for (Result<Item> result : results) {
                items.add(result.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return items;
    }

    /**
     * 删除
     */
    public boolean remove(String fileName){
        try {
            minioClient.removeObject( RemoveObjectArgs.builder().bucket(prop.getBucketName()).object(fileName).build());
        }catch (Exception e){
            return false;
        }
        return true;
    }
}
