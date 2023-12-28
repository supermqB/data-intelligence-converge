package com.lrhealth.data.converge.common.enums;

import org.springframework.data.relational.core.sql.In;

import java.util.Objects;

/**
 * 治理任务状态
 *
 * @author lr
 * @since 2022-11-22
 */
public enum OdsDataSizeEnum {

    ADMISSION_ASSESSMENT("admission_assessment", 2266),
    ADMISSION_RECORD("admission_record",	4874),
    AI_AD_RECORD("ai_ad_record",	0),
    AI_AT_RECORD("ai_at_record",	0),
    AMSSTT_RECORD("amsstt_record",	0),
    ANESTHESIA_RECORDS("anesthesia_records",	1510),
    ANESTHESIA_RECORDS_DETAILS("anesthesia_records_details",	622),
    AS_RECORD("as_record",	0),
    BASE_MEDI_PRE_PAY("base_medi_pre_pay",	397),
    BLOOD_INVENTORY_INFO("blood_inventory_info",	392),
    CAESAREAN_SECTION_RECORD("caesarean_section_record",	3542),
    CARE_PLAN("care_plan",	841),
    CHINESE_DIAG_RECORD("chinese_diag_record",	903),
    CHINESE_PRESCRIPTION("chinese_prescription",	1695),
    CLINICAL_PATHWAY_RECORD("clinical_pathway_record",	695),
    CONSULTATION_RECORD("consultation_record",	3355),
    CSCCM_RECORD("csccm_record",	0),
    CSE_INFORMED_CONSENT("cse_informed_consent",	8779),
    DAILY_COURSE_RECORD("daily_course_record",	2314),
    DEATH_CASE_DISCUSSION_RECORD("death_case_discussion_record",	4999),
    DEATH_RECORD("death_record",	5067),
    DEPT_INFO("dept_info",	347),
    DI_ADV_ADE_INFO("di_adv_ade_info",	1597),
    DI_ADV_SAE_INFO("di_adv_sae_info",	1053),
    DI_HDI_LARDRU_LIST("di_hdi_lardru_list",	544),
    DI_HDI_LARGERM_LIST("di_hdi_largerm_list",	546),
    DI_HIC_INFECTION_INFO("di_hic_infection_info",	1273),
    DI_HIC_OPEREC_INFO("di_hic_operec_info",	1760),
    DI_REF_PROREQ_INFO("di_ref_proreg_info",	969),
    DI_RSG_BEDS_INFO("di_rsg_beds_info",	501),
    DI_RSG_FACILITY_INFO("di_rsg_facility_info",	659),
    DI_RSG_FIXEDASSETS_INFO("di_rsg_fixedassets_info",	1314),
    DICT_INV("dict_inv",	948),
    DISCHARGE_ABSTRACT("discharge_abstract",	2448),
    DISCHARGE_ASSESSMENT("discharge_assessment",	1581),
    DISCHARGE_RECORD("discharge_record",	7270),
    DMVA_RECORD("dmva_record",	0),
    DRUG_ADJ_HIS("drug_adj_his",	809),
    DRUG_DETAIL_INFO("drug_detail_info",	695),
    DRUG_DISTRIBUTION_RECORD("drug_distribution_record",	854),
    DRUG_INVENTORY_INFO("drug_inventory_info",	817),
    DRUG_PUR_DETAIL("drug_pur_detail",	947),
    DRUG_SUPPLIER_INFO("drug_supplier_info",	488),
    DSH_RECORD("dsh_record",	0),
    EMERGENCY_MEDICAL_RECORD("emergency_medical_record",	1479),
    EXPECTANT_RECORD("expectant_record",	1347),
    FIRST_COURSE_RECORD("first_course_record",	5841),
    GR_RECORD("gr_record",	0),
    H24_ADMISSION_DISCHARGE_RECORD("h24_admission_discharge_record",	2084),
    HANDOVER_RECORD("handover_record",	5706),
    HOSPITAL_INFO("hospital_info",	895),
    HOSPITALIZED_DIAGNOSIS("hospitalized_diagnosis",	661),
    HOSPITALIZED_DISPENSE_DETAIL("hospitalized_dispense_detail",	882),
    HOSPITALIZED_SETTLEMENT("hospitalized_settlement",	1385),
    HOURSE24_ADMISSION_DEATH_RECORD("hours24_admission_death_record",	6789),
    HSCT_RECORD("hsct_record",	0),
    IAT_RECORD("iat_record",	0),
    INPATIENT_MEDICAL_RECORD("inpatient_medical_record",	9184),
    INSPECTION_RECORD("inspection_record",	2047),
    INTAKE_OUTPUT_DURG_DETAIL("intake_output_durg_detail",	586),
    INTAKE_OUTPUT_RECORD("intake_output_record",	807),
    INTRACTABLE_DISCUSSRECORD("intractable_discussrecord",	5506),
    LALT_RECORD("lalt_record",	0),
    MEDICAL_EXAMINATION_RECORD("medical_examination_record",	0),
    MEDICAL_STAFF_INFO("medical_staff_info",	524),
    MONITOR_VITAL_SIGNS_RECORD("monitor_vital_signs_record",	976),
    OMTCCR_RECORD("omtccr_record",	0),
    OPERATION_CARE_RECORD("operation_care_record",	1194),
    OPERATION_INFORMED_CONSENT("operation_informed_consent",	4563),
    OPERATION_RECORD("operation_record",	1710),
    OTHER_INFORMED_CONSENT("other_informed_consent",	2403),
    OUTPATIENT_DISPENSE_RECORD("outpatient_dispense_record",	725),
    PATIENT("patient",	1353),
    PAY_ITEMS("pay_items",	580),
    PHASE_SUMMARY("phase_summary",	4866),
    PHIAR_RECORD("phiar_record",	0),
    POSTOPERAT_FIRST_COURSE("postoperat_first_course",	3088),
    PRE_OPERATION_DISCUSSION("pre_operation_discussion",	5122),
    PAY_IPRE_OPERATION_SUMMARYTEMS("pay_ipre_operation_summarytems",	4194),
    PRECIOUS_CONSUM_USAGE_RECORD("precious_consum_usage_record",	791),
    REGISTRATION_RECORD("registration_record",	731),
    RSIT_RECORD("rsit_record",	0),
    SALVAGE_RECORD("salvage_recrod",	3787),
    SECTION_NB_RECORD("section_nb_record",	2279),
    SPECIAL_TREATMENT_CONSENT("special_treatment_consent",	1238),
    STAFF_INFO("staff_info",	921),
    SUPERIOR_DOCTOR_WARDROUND("superior_doctor_wardround",	3431),
    TAT_RECORD("tat_record",	0),
    TRANSFERENCE_RECORD("transference_record",	7441),
    TRANSFUSION_BLOOD_RECORD("transfusion_blood_record",	1127),
    TRANSFUSION_INFORMED_CONSENT("transfusion_informed_consent",	4104),
    VBNC_RECORD("vbnc_record",	3550),
    VISITS_REC_AFT_ANESTHESIA("visits_rec_aft_anesthesia",	881),
    VISITS_REC_BEF_ANESTHESIA("visits_rec_bef_anesthesia",	1107),
    WARD_WORK_LOG_DETAIL_LIST("ward_work_log_detail_list",	562),
    WARD_WORK_LOG_LIST("ward_work_log_list",	640),
    WESTERN_PRESCRIPTION("western_prescription",	1592),
    WORSE_PATIENT_CARE_RECORD("worse_patient_care_record",	968),
    WOUND_QUALITY_CONTROL_MANAGE("wound_quality_control_manage",	0),
    YY_BUSI_BASIC_NUMBER("yy_busi_basic_number",	349),
    DI_RSG_DRUGALTER_INFO("di_rsg_drugalter_info",	1025),
    HOSPITALIZED_ORDER("hospitalized_order",	1117),
    SURVEY_RECORD("survey_record",	1775),
    OUTPATIENT_EMERG_SETTLEMENT("outpatient_emerg_settlement",	859),
    HOSPITALIZED_EXPSET_DETAIL("hospitalized_expset_detail",	714),
    TREATMENT_RECORD("treatment_record",	1009),
    TRANSFUSION_RECORD("transfusion_record",	674),
    HOS_EDU("hos_edu",	484),
    CRITICALLY_ILL_NOTICE("critically_ill_notice",	5357),
    DICT_EQUI_KIND("dict_equi_kind",	0),
    DI_IDN_INDNOT_INFO("di_idn_indnot_info",	1487),
    DI_ADV_MSDE_INFO("di_adv_msde_info",	770),
    GENERAL_CARE_RECORD("general_care_record",	1155),
    MONITOR_VITAL_SIGNS_DETAIL("monitor_vital_signs_detail",	524),
    OUTPATIENT_EMERGENCY_RECORD("outpatient_emergency_record",	1996),
    INPATIENT_TCM_RECORD("inpatient_tcm_record",	10173),
    MAPPING_RECORD("mapping_record",	0);





    /**
     * 变量分类对应值
     */

    private final String tableName;
    private final Integer value;


    OdsDataSizeEnum(String tableName, Integer value) {
        this.value = value;
        this.tableName = tableName;
    }

    public static OdsDataSizeEnum of(String tableName) {
        for (OdsDataSizeEnum taskEnum : OdsDataSizeEnum.values()) {
            if (Objects.equals(taskEnum.tableName, tableName)) {
                return taskEnum;
            }
        }
        return null;
    }

    public Integer getValue() {
        return value;
    }


    public static Integer getValue(String tableName) {
        Integer value = null;
        for (OdsDataSizeEnum taskEnum : OdsDataSizeEnum.values()) {
            if (Objects.equals(taskEnum.tableName, tableName)) {
                value =  taskEnum.getValue();
            }
        }
        return (value == null || value == 0) ? 100 : value;
    }
}
