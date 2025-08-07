package com.lrhealth.data.converge.common.util;

import cn.hutool.crypto.asymmetric.RSA;

public class RsaUtils {
    private static RSA rsa;

    public static RSA getInstance(String key) {
        if (rsa == null || !key.equals(rsa.getPrivateKeyBase64())) {
            rsa = new RSA(key, null);
        }
        return rsa;
    }
}
