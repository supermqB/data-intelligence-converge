package com.lrhealth.data.converge.common.util;

import cn.hutool.core.text.CharPool;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jinmengyu
 * @date 2025-04-22
 */
public class ElementFormatUtil {

    public static final String STRING_ELEMENT = "S1";
    public static final String STRING_ELEMENT_FORMAT = "AN..{LENGTH}";
    public static final String NUMBER_ELEMENT = "N";
    public static final String NUMBER_ELEMENT_FORMAT = "N..{LENGTH}{DECIMAL}";
    public static final String DATE_ELEMENT = "D";
    public static final String DATE_ELEMENT_FORMAT = "D8";
    public static final String DATETIME_ELEMENT = "DT";
    public static final String DATETIME_ELEMENT_FORMAT = "DT15";
    public static final String TIME_ELEMENT = "T";
    public static final String TIME_ELEMENT_FORMAT = "T6";
    public static final String BINARY_ELEMENT = "BY";

    private static final Map<String, String> ELEMENT_FORMAT_MAP = new HashMap<>();
    private static final Map<Integer, FieldType> FIELD_FORMAT_MAP = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(ElementFormatUtil.class);

    static {
        ELEMENT_FORMAT_MAP.put(STRING_ELEMENT, STRING_ELEMENT_FORMAT);
        ELEMENT_FORMAT_MAP.put(NUMBER_ELEMENT, NUMBER_ELEMENT_FORMAT);
        ELEMENT_FORMAT_MAP.put(DATE_ELEMENT, DATE_ELEMENT_FORMAT);
        ELEMENT_FORMAT_MAP.put(DATETIME_ELEMENT, DATETIME_ELEMENT_FORMAT);
        ELEMENT_FORMAT_MAP.put(TIME_ELEMENT, TIME_ELEMENT_FORMAT);
        ELEMENT_FORMAT_MAP.put(BINARY_ELEMENT, STRING_ELEMENT_FORMAT);
    }

    private ElementFormatUtil(){
    }
    static {
        Field[] declaredFields = Types.class.getDeclaredFields();
        for (Field field : declaredFields){
            try {
                Integer dataType = (Integer) field.get(Types.class);
                FIELD_FORMAT_MAP.put(dataType, new FieldType(dataType));
            } catch (IllegalAccessException e) {
                log.error("启动加载java数据类型失败， {}", ExceptionUtils.getStackTrace(e));
            }
        }
    }


    public static String getElement(Integer dataType){
        switch (dataType){
            case Types.BIT:
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.ROWID:
                return NUMBER_ELEMENT;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.BOOLEAN:
                return STRING_ELEMENT;
            case Types.DATE:
                return DATE_ELEMENT;
            case Types.TIME:
            case Types.TIME_WITH_TIMEZONE:
                return TIME_ELEMENT;
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return DATETIME_ELEMENT;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
            case Types.CLOB:
            case Types.NCLOB:
                return BINARY_ELEMENT;
            default:
                return STRING_ELEMENT;
        }
    }

    public static String getElementFormat(Integer dataType, Integer length){
        FieldType fieldType = FIELD_FORMAT_MAP.get(dataType);
        String elementFormat = fieldType.getElementFormat();
        String replaced = elementFormat.replace("{LENGTH}", length.toString());
        if (NUMBER_ELEMENT.equals(fieldType.getElementType())){
            switch (dataType){
                case Types.FLOAT:
                case Types.REAL:
                    return replaced.replace("{DECIMAL}", CharPool.COMMA + "2");
                case Types.DOUBLE:
                case Types.DECIMAL:
                case Types.NUMERIC:
                    return replaced.replace("{DECIMAL}", CharPool.COMMA + "4");
                default:
                    return replaced.replace("{DECIMAL}", "");
            }
        }
        return replaced;
    }

    @Data
    private static class FieldType{
        private Integer sqlDataType;
        private String javaFieldType;
        private String elementType;
        private String elementFormat;
        public FieldType(Integer sqlDataType){
            this.sqlDataType = sqlDataType;
            this.javaFieldType = DatabaseTypeUtil.getJavaType(sqlDataType);
            this.elementType = getElement(sqlDataType);
            this.elementFormat = ELEMENT_FORMAT_MAP.get(elementType);
        }
    }


}
