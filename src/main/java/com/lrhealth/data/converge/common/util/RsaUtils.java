package com.lrhealth.data.converge.common.util;

import cn.hutool.crypto.asymmetric.RSA;

/**
 * @author zhaohui
 * @version 1.0
 * @description: TODO
 * @date 2023/9/19 14:00
 */
public class RsaUtils {
    private static RSA rsa;

    public static RSA getInstance(String key) {
        if(rsa == null || !key.equals(rsa.getPrivateKeyBase64())){
            rsa = new RSA(key,null);
        }
        return rsa;
    }
}
