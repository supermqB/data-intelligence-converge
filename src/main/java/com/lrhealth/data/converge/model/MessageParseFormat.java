package com.lrhealth.data.converge.model;

import cn.hutool.core.text.StrPool;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author jinmengyu
 * @date 2024-08-28
 */
@Data
@Component
@ConfigurationProperties(prefix = "message.body")
public class MessageParseFormat {

    private String operation;
    private String preValue;
    private String postValue;
    private String collectTable;

    private String insertOp;
    private String updateOp;
    private String deleteOp;
    private String manageOp;

    public String matchOperationKey(Map<String, Object> valueMap){
        String[] opList = operation.split(StrPool.COMMA);
        for(String op : opList){
            if (valueMap.containsKey(op)){
                return op;
            }
        }
        return null;
    }

    public String matchPreValueKey(Map<String, Object> valueMap){
        String[] opList = preValue.split(StrPool.COMMA);
        for(String op : opList){
            if (valueMap.containsKey(op)){
                return op;
            }
        }
        return null;
    }

    public String matchCollectTableKey(Map<String, Object> valueMap){
        String[] collectTableList = collectTable.split(StrPool.COMMA);
        for(String tablePre : collectTableList){
            if (valueMap.containsKey(tablePre)){
                return tablePre;
            }
        }
        return null;
    }

    public String matchPostValueKey(Map<String, Object> valueMap){
        String[] opList = postValue.split(StrPool.COMMA);
        for(String op : opList){
            if (valueMap.containsKey(op)){
                return op;
            }
        }
        return null;
    }

    public boolean isInsertOp(String op){
        String[] insertList = insertOp.split(StrPool.COMMA);
        for(String insert : insertList){
           if (insert.equalsIgnoreCase(op)){
               return true;
           }
        }
        return false;
    }

    public boolean isUpdateOp(String op){
        String[] updateList = updateOp.split(StrPool.COMMA);
        for(String update : updateList){
            if (update.equalsIgnoreCase(op)){
                return true;
            }
        }
        return false;
    }

    public boolean isDeleteOp(String op){
        String[] deleteList = deleteOp.split(StrPool.COMMA);
        for(String delete : deleteList){
            if (delete.equalsIgnoreCase(op)){
                return true;
            }
        }
        return false;
    }

    public boolean isManageOp(String op){
        String[] manageList = manageOp.split(StrPool.COMMA);
        for(String management : manageList){
            if (management.equalsIgnoreCase(op)){
                return true;
            }
        }
        return false;
    }
}
