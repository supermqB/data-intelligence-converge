package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.service.OdsModelService;
import com.lrhealth.data.model.original.model.OriginalModel;
import com.lrhealth.data.model.original.model.OriginalModelColumn;
import com.lrhealth.data.model.original.service.OriginalModelColumnService;
import com.lrhealth.data.model.original.service.OriginalModelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.Key;
import java.security.SecureRandom;
import java.util.List;

/**
 * @author jinmengyu
 * @date 2023-08-21
 */
@Service
@Slf4j
public class OdsModelServiceImpl implements OdsModelService {

    @Resource
    private OriginalModelService originalModelService;
    @Resource
    private OriginalModelColumnService modelColumnService;

    @Override
    public String getTableDataType(String odsTableName, String sysCode) {
        List<OriginalModel> tableList = originalModelService.list(new LambdaQueryWrapper<OriginalModel>()
                .eq(CharSequenceUtil.isNotBlank(odsTableName), OriginalModel::getNameEn, odsTableName)
                .eq(CharSequenceUtil.isNotBlank(sysCode), OriginalModel::getSysCode, sysCode)
                .eq(OriginalModel::getDelFlag, 0));
        if (tableList.size() > 1){
            throw new CommonException("originalModel查询{}错误", odsTableName);
        }
        if (CollUtil.isEmpty(tableList)){
            return null;
        }
        return tableList.get(0).getDataType();
    }
    @Override
    public List<OriginalModelColumn> getColumnList(String odsModelName, String sysCode) {
        List<OriginalModel> originalModel = originalModelService.list(new LambdaQueryWrapper<OriginalModel>()
                .eq(OriginalModel::getNameEn, odsModelName).eq(OriginalModel::getSysCode, sysCode)
                .eq(OriginalModel::getDelFlag, 0));
        if (originalModel.size() != 1){
            throw new CommonException("原始模型数据错误:{}", odsModelName + "-" + sysCode);
        }
        return modelColumnService.list(new LambdaQueryWrapper<OriginalModelColumn>()
                .eq(OriginalModelColumn::getModelId, originalModel.get(0).getId())
                .eq(OriginalModelColumn::getDelFlag, 0));
    }

    @Override
    public List<OriginalModel> getModelList(List<Long> modelIdList) {
       return originalModelService.list(new LambdaQueryWrapper<OriginalModel>().in(OriginalModel::getId,modelIdList)
                .eq(OriginalModel::getDelFlag,0));
    }

    public static void main(String[] args) {
        String original="LRSH@2022";
        System.out.println("原文=\t"+original);

        try {
            // 生产密钥
            String password="PEB123@321BEP";// 口令
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
            SecretKeyFactory factory=SecretKeyFactory.getInstance("PBEWITHMD5andDES");
            Key key =factory.generateSecret(pbeKeySpec);// 密钥，下面加密解密都要用到
            System.out.println("密钥=\t"+Base64.encodeBase64String(key.getEncoded()));

            // 初始化盐
            SecureRandom random=new SecureRandom();
            byte [] salt=random.generateSeed(8);
            PBEParameterSpec pbeParameterSpec=new PBEParameterSpec(salt, 100);

            // 加密
            Cipher cipher =Cipher.getInstance("PBEWITHMD5andDES");
            cipher.init(Cipher.ENCRYPT_MODE,key, pbeParameterSpec);
            byte[] bytes = cipher.doFinal(original.getBytes());
            System.out.println("密文=\t"+ Base64.encodeBase64String(bytes));

            // 解密
            cipher.init(Cipher.DECRYPT_MODE,key,pbeParameterSpec);
            bytes=cipher.doFinal(bytes);
            System.out.println("解密后=\t"+new String(bytes));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
