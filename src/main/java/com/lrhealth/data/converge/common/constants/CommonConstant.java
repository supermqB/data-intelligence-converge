package com.lrhealth.data.converge.common.constants;

/**
 * <p>
 * 通用常量类
 * </p>
 *
 * @author lr
 * @since 2023/5/8 14:28
 */
public class CommonConstant {
    private CommonConstant() {

    }

    /**
     * valueRange 特点代码值以‘dict_’开始列表示值域字段,该值表示值域表名
     */
    public static final String DICT_PREFIX = "dict_";

    /**
     * 默认用户
     */
    public static final String DEFAULT_USER = "sys";

    /**
     * 数据模型
     */
    public static final String DATA_MODEL_DWD = "dwd";

    public static final String PRODUCER_TOPIC = "bigdata_standard";

    public static final String PRODUCER_SYNC_KEY = "data-sync";
    public static final String CONSUMER_GROUP_SUFFIX = "-consumer-group";

    /**
     * kafka消费端唯一ID
     */
    public static String CONSUMER_ID;

    /**
     * 任务分发时使用了redis，这个key用来保证redis读写原子性
     */
    public static final String TASK_DISTRIBUTE_LOCK_KEY = "task_distribute_lock_key";

    /**
     * 任务分发时使用的redis的key的前辍
     */
    public static final String TASK_DISTRIBUTE_REDIS_KEY_PREFIX = "task_dist_prefix:";

    /**
     * 任务分发时使用的task等待队列的redis-key
     */
    public static final String TASKID_LIST_KEY = "taskid_list_key";

    /**
     * 任务分发时使用的高优先级task等待队列的redis-key
     */
    public static final String TASKID_LIST_PRIORITY_KEY = "taskid_list_priority_key";

    /**
     * 任务分发时使用的任务列表的key的前辍
     */
    public static final String HPS_TASKID_LIST_KEY_PREFIX = "hps_taskid_list_key_prefix:";

    /**
     * 编码
     */
    public static final String CODE = "code";

    public static final String XDS_ID = "xds_id";

    public static final String COUNT = "count";

    public static final String MOBILE = "mobile";

    public static final String NO_MOBILE = "!mobile";

    public static final String ANNUAL_CHANGE = "年差";

    public static final String DIURNAL_DIFFERENCE = "日差";

    public static final String YEAR = "year";

    public static final String DAY = "day";

    public static final String SYS_SCENE = "医疗服务";
    /**
     * 原始表表前缀
     */
    public static final String TAB_NAME_PRE_ODS = "gove_ods_";
    /**
     * 原始表表前缀
     */
    public static final String TAB_NAME_PRE_DWD = "gove_dwd_";

    /**
     * 配置表表前缀
     */
    public static final String TAB_NAME_PRE_CONF = "gove_conf_";
    /**
     * 治理表表前缀
     */
    public static final String TAB_NAME_PRE_GOVERNANCE = "gove_";
    /**
     * 对象识别表表前缀
     */
    public static final String TAB_NAME_PRE_OBJ = "obj_";
    /**
     * 标准表表前缀
     */
    public static final String TAB_NAME_PRE_STD = "std_";
    /**
     * 任务调度表表前缀
     */
    public static final String TAB_NAME_PRE_TASK = "sche_";
    /**
     * 审核表表前缀
     */
    public static final String TAB_NAME_PRE_AUDIT = "susp_";

    /**
     * 字段名
     */
    public static final String DWD_TAB_PRI_KEY = "event_id";

    public static final String TECH_FIELD_BATCH_NUM = "batch_num";

    public static final String TECH_FIELD_BUSINESS_ID = "business_id";

    public static final String TECH_FIELD_MANUAL_FLAG = "manual_flag";

    public static final String TECH_FIELD_HOSPITAL_CODE = "hospital_code";

    public static final String TECH_FIELD_EVENT_ID = "event_id";

    public static final String TECH_FIELD_EVENT_TYPE_CODE = "event_type_code";

    public static final String TECH_FIELD_EVENT_TYPE_NAME = "event_type_name";

    public static final String TECH_FIELD_DATA_CREATE_TIME = "data_create_time";

    public static final String TECH_FIELD_DATA_MODIFY_TIME = "data_modify_time";

    public static final String TECH_FIELD_ROW_MD5 = "row_md5";

    public static final String TECH_FIELD_PRIMARY_ABSTRACT = "primary_abstract";

    public static final String TECH_FIELD_INTERACTION_ABSTRACT = "interaction_abstract";

    public static final String TECH_FIELD_QC_ABSTRACT_BEFORE = "qc_abstract_before";

    public static final String TECH_FIELD_QC_ABSTRACT_AFTER = "qc_abstract_after";

    public static final String ODS_ROW_ID_FIELD_NAME = "row_id";

    public static final String EVENT_TABLE_NAME = "hevent_record";

    public static final String DWD_ID_FIELD_NAME = "id";

    public static final String GOVE_TASK_LISTENER_ID = "task";

    public static final String GOVE_PRIORITY_TASK_LISTENER_ID = "priority-task";
}
