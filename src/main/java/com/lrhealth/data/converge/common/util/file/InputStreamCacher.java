package com.lrhealth.data.converge.common.util.file;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/** 
 * 缓存InputStream，以便InputStream的重复利用 
 * @author wudi
 * @version 2022-08-18
 */
@Slf4j
public class InputStreamCacher {
      
    /** 
     * 将InputStream中的字节保存到ByteArrayOutputStream中。 
     */  
    private ByteArrayOutputStream byteArrayOutputStream = null;
      
    public InputStreamCacher(InputStream inputStream) {  
        if (ObjectUtils.isNull(inputStream))  {
            return;
        }
          
        byteArrayOutputStream = new ByteArrayOutputStream();  
        byte[] buffer = new byte[1024];    
        int len;
        try {
            while ((len = inputStream.read(buffer)) > -1 ) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            byteArrayOutputStream.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
      
    public InputStream getInputStream() {
        if (ObjectUtils.isNull(byteArrayOutputStream))  {
            return null;
        }
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());  
    }
}  