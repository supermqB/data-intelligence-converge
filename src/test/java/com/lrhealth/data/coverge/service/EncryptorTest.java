package com.lrhealth.data.coverge.service;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.junit.jupiter.api.Test;

public class EncryptorTest {


    @Test
    public void test() {
        System.out.println(encrypt("LR_rdcp@2023"));
        System.out.println(encrypt("root@rdcp_std"));

        System.out.println(decrypt("xnEjWjSiD0F7GiGKY0X/svhDm+KZFtdT"));
        System.out.println(decrypt("6N0W2cLkeW58Yd+R9z2Tslos10Dg6+If"));
        System.out.println(decrypt("5Ngf637dUzz2xRz4RewABd5tGBaCPMA/"));

//        String decryptPassword = decrypt("");
//        System.out.println(decryptPassword);

    }

    /**
     * 加密
     * @param plaintext 明文密码     * @return
     */
    public static String encrypt(String plaintext) {
        //加密工具
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        //加密配置
        EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
        // 算法类型
        config.setAlgorithm("PBEWithMD5AndDES");
        //生成秘钥的公钥
        config.setPassword("PEB123@321BEP");
        //应用配置
        encryptor.setConfig(config);
        //加密
        String ciphertext = encryptor.encrypt(plaintext);
        return ciphertext;
    }

    /**
     * 解密
     *
     * @param ciphertext 待解密秘钥
     * @return
     */
    public static String decrypt(String ciphertext) {
        //加密工具
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        //加密配置
        EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
        config.setAlgorithm("PBEWithMD5AndDES");
        //生成秘钥的公钥
        config.setPassword("PEB123@321BEP");
        //应用配置
        encryptor.setConfig(config);
        //解密
        String pText = encryptor.decrypt(ciphertext);
        return pText;
    }
}
