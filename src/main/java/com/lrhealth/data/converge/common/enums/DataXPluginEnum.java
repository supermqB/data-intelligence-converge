package com.lrhealth.data.converge.common.enums;

import cn.hutool.core.text.CharSequenceUtil;
import com.lrhealth.data.converge.common.exception.CommonException;

/**
 * @author jinmengyu
 * @date 2023-08-31
 */
public enum DataXPluginEnum {
    /**
     * datax插件枚举
     */
    ORACLE("oracle", "oracle"),
    POSTGRESQL("postgresql", "postgresql"),
    FILE("file", "txtfile"),
    MYSQL("mysql", "mysql"),
    OCEANBASE("oceanbase", "oceanbasev10");

    private final String database;

    private final String dataXPlugin;

    public String getPlugin(){return this.dataXPlugin;}

    public String getDatabase(){return this.database;}

    DataXPluginEnum(String database, String dataXPlugin) {
        this.database = database;
        this.dataXPlugin = dataXPlugin;
    }

    public static String getDatabasePlugin(String database){
        if (CharSequenceUtil.isBlank(database)) {
            throw new CommonException("状态参数为空");
        }
        for (DataXPluginEnum dataXPlugin : DataXPluginEnum.values()) {
            if (database.equals(dataXPlugin.database)) {
                return dataXPlugin.dataXPlugin;
            }
        }
        throw new CommonException("未找到匹配的状态值");
    }

}
