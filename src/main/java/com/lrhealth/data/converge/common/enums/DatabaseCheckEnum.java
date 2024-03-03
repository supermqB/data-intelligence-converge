package com.lrhealth.data.converge.common.enums;

import static com.lrhealth.data.converge.common.constants.FepConstant.DEFAULT_SQL;

/**
 * @author jinmengyu
 * @date 2024-01-02
 */
public enum DatabaseCheckEnum {


    MYSQL("mysql", DEFAULT_SQL),
    POSTGRESQL("postgresql", DEFAULT_SQL),
    SQLSERVER("sqlserver", DEFAULT_SQL),
    ORACLE("oracle", "select 1 from dual"),
    IRIS("IRIS", DEFAULT_SQL),
    OCEANBASE("oceanbase", DEFAULT_SQL),
    HIVE("hive2", DEFAULT_SQL)
    ;

    private final String database;

    private final String checkSql;

    DatabaseCheckEnum(String database, String checkSql){
        this.database = database;
        this.checkSql = checkSql;
    }

    public static String getCheckSql(String database){
        for (DatabaseCheckEnum databaseCheckEnum : DatabaseCheckEnum.values()){
            if (database.equals(databaseCheckEnum.database)){
                return databaseCheckEnum.checkSql;
            }
        }
        return null;
    }

}
