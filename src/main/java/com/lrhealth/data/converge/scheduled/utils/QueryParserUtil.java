package com.lrhealth.data.converge.scheduled.utils;

import cn.hutool.core.util.ReUtil;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author jinmengyu
 * @date 2023-09-15
 */
public class QueryParserUtil {

    private QueryParserUtil(){}

    private static final String SQL_QUERY_TEMPLATE = "select(.*)from";

    private static final String POSTGRESQL_JDBC_TEMPLATE = "";

    public static List<String> queryColumnParser(String sqlQuery){
//        String fieldPlaceholder = ReUtil.get(SQL_QUERY_TEMPLATE, "\\{([^}]+)}", 1);
//        String fields = sqlQuery.replace(fieldPlaceholder, "").trim();
//        return Arrays.asList(fields.split(","));
        String s = ReUtil.get(SQL_QUERY_TEMPLATE, sqlQuery, 1);
        String[] split = s.trim().split(",");
        return Arrays.stream(split).map(column -> {
            return ReUtil.get(".*\\.(.*)", column, 1);
        }).collect(Collectors.toList());
    }


    public static void main(String[] args) {
        String sql = "select\n" +
                "\tt1.clinic_organ_code,\n" +
                "\tt1.clinic_organ_name,\n" +
                "\tt1.clinic_dept_code,\n" +
                "\tt1.clinic_dept_name,\n" +
                "\tt1.patient_local_id,\n" +
                "\tt1.prescribe_serial_no,\n" +
                "\tt1.prescribe_no,\n" +
                "\tt1.prescription_note_inf,\n" +
                "\tt1.prescription_drug_pharm_sig,\n" +
                "\tt1.prescription_check_sig,\n" +
                "\tt1.prescription_medicine_no,\n" +
                "\tt1.outpatient_no,\n" +
                "\tt1.visit_count,\n" +
                "\tt1.name,\n" +
                "\tt1.age_year,\n" +
                "\tt1.age_month,\n" +
                "\tt1.gender_code,\n" +
                "\tt1.prescription_dep_code,\n" +
                "\tt1.prescription_dep_name,\n" +
                "\tt1.prescribe_input_date,\n" +
                "\tt1.prescribe_input_sig,\n" +
                "\tt1.prescribe_chk_sig,\n" +
                "\tt1.prescribe_dispen_sig,\n" +
                "\tt1.doctor_code,\n" +
                "\tt1.doctor_name,\n" +
                "\tt1.prescription_identifier_no,\n" +
                "\tt1.prescription_item_class_code,\n" +
                "\tt1.prescription_item_class_name,\n" +
                "\tt1.prescription_detail_code,\n" +
                "\tt1.prescription_detail_name,\n" +
                "\tt1.trialparty_pha_code,\n" +
                "\tt1.trialparty_pha_name,\n" +
                "\tt1.disease_diagnosis_code,\n" +
                "\tt1.drug_id,\n" +
                "\tt1.drug_name,\n" +
                "\tt1.drug_specifications,\n" +
                "\tt1.drug_dosage_code,\n" +
                "\tt1.drug_dosage_name,\n" +
                "\tt1.drug_use_dose,\n" +
                "\tt1.drug_use_dose_unit,\n" +
                "\tt1.drug_use_frequency_code,\n" +
                "\tt1.drug_use_frequency,\n" +
                "\tt1.drug_use_route_code,\n" +
                "\tt1.drug_use_route_name,\n" +
                "\tt1.drug_use_total_dose,\n" +
                "\tt1.drug_use_days,\n" +
                "\tt1.yplb,\n" +
                "\tt1.skin_test_criterion,\n" +
                "\tt1.is_unified_procurement_drugs,\n" +
                "\tt1.base_drug_code,\n" +
                "\tt1.insur_code,\n" +
                "\tt1.drug_procurement_code,\n" +
                "\tt1.antibacterials_flag,\n" +
                "\tt1.prescription_drug_amount,\n" +
                "\tt1.drugs_unit,\n" +
                "\tt1.unit_price,\n" +
                "\tt1.tot_amount,\n" +
                "\tt1.quantity,\n" +
                "\tt1.medication_start_time,\n" +
                "\tt1.medication_stop_time,\n" +
                "\tt1.medication_days,\n" +
                "\tt1.prescribe_days,\n" +
                "\tt1.antiba_drugs_level,\n" +
                "\tt1.antiba_drugs_level_name,\n" +
                "\tt1.medicine_type_code,\n" +
                "\tt1.medicine_type_name,\n" +
                "\tt1.if_main_medicine,\n" +
                "\tt1.if_base_medicine,\n" +
                "\tt1.adaptive_flag,\n" +
                "\tt1.urgent_flag,\n" +
                "\tt1.med_view_flag,\n" +
                "\tt1.drug_code,\n" +
                "\tt1.skin_test_result,\n" +
                "\tt1.reg_sn,\n" +
                "\tt1.settlement_sn,\n" +
                "\tt1.resund_iden,\n" +
                "\tt1.dispensing_no,\n" +
                "\tt1.drug_withdrawal_iden,\n" +
                "\tt1.create_date,\n" +
                "\tt1.update_date,\n" +
                "\tt1.upload_time,\n" +
                "\tt1.estatus,\n" +
                "\tt1.prescribe_input_date_day,\n" +
                "\tt1.center_upload_time,\n" +
                "\tt2.outpatient_flag\n" +
                "from\n" +
                "\twestern_prescription t1\n" +
                "left join registration_record t2 on\n" +
                "\tt1.outpatient_no = t2.outpatient_no";
        String s = ReUtil.get("select(.*)from", sql, 1);
        String[] split = s.trim().split(",");
        List<Object> collect = Arrays.stream(split).map(column -> {
            return ReUtil.get(".*\\.(.*)", column, 1);
        }).collect(Collectors.toList());
        System.out.println(collect);
    }

    public static String getDbType(String url) {
        Pattern p = Pattern.compile("jdbc:(?<db>\\w+):.*((//)|@)(?<host>.+):(?<port>\\d+)(/|(;DatabaseName=)|:)(?<dbName>\\w+)\\??.*");
        Matcher m = p.matcher(url);
        String db = null;
        if(m.find()) {
            db = m.group("db");
            String host = m.group("host");
            String port = m.group("port");
            String dbName = m.group("dbName");
        }
        return db;
    }
}
