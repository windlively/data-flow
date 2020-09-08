package test.ink.andromeda.dataflow.test;

import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Map;

import static ink.andromeda.dataflow.util.GeneralUtils.GSON;

public class StaticTest {


    @Test
    public void gsonTest(){
        Map<String, Object> map = GSON().fromJson("{\n" +
                "    \"_id\" : ObjectId(\"5f3e14d6a9b8224852a57642\"),\n" +
                "    \"schema\" : \"abak\",\n" +
                "    \"converter\" : [ \n" +
                "        {\n" +
                "            \"dest_table\" : \"core_lm_loan\",\n" +
                "            \"eval_context\" : [ \n" +
                "                {\n" +
                "                    \"sql\" : \"select loan.loan_no, nvl(loan.orig_prcp,0)+nvl(loan2.orig_prcp, 0) as loan_amount, nvl(loan.orig_prcp, 0) as haier_amount, nvl(loan2.orig_prcp, 0) as capital_amount from glloans_haierdb.v_lm_loan loan left join plus_glloans.v_lm_loan loan2 on loan2.loan_cont_no = loan.loan_cont_no where loan.loan_no = '${#LOAN_NO}' \",\n" +
                "                    \"type\" : \"map\",\n" +
                "                    \"data_source\" : \"haier\",\n" +
                "                    \"name\" : \"otherAmount\"\n" +
                "                }, \n" +
                "                {\n" +
                "                    \"sql\" : \"select lap.loan_mode as LOAN_MODE  from GLLOANS_HAIERDB.v_lm_loan lo, CMIS_HAIERDB.v_lc_appl lap where lo.loan_cont_no=lap.cont_no and lo.loan_no='${#LOAN_NO}' \",\n" +
                "                    \"type\" : \"map\",\n" +
                "                    \"data_source\" : \"haier\",\n" +
                "                    \"name\" : \"LM\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"simple_class_convert\" : true,\n" +
                "            \"simple_value_convert\" : {\n" +
                "                \"LOAN_MODE\" : \"[LM] == null ? null : [LM][LOAN_MODE]\",\n" +
                "                \"LOAN_AMOUNT\" : \"[otherAmount] == null? 0 : ([otherAmount][LOAN_AMOUNT]?:0)\",\n" +
                "                \"HAIER_AMOUNT\" : \"[otherAmount] == null ? 0: ([otherAmount][HAIER_AMOUNT]?:0)\",\n" +
                "                \"CAPITAL_AMOUNT\" : \"[otherAmount] == null ? 0: ([otherAmount][CAPITAL_AMOUNT]?:0)\",\n" +
                "                \"update_time\" : \"null\",\n" +
                "                \"create_time\" : \"null\"\n" +
                "            },\n" +
                "            \"custom_operation\" : [],\n" +
                "            \"conditional\" : [],\n" +
                "            \"db_update\" : {\n" +
                "                \"method\" : \"auto\",\n" +
                "                \"insert\" : {\n" +
                "                    \"auto_id\" : true\n" +
                "                },\n" +
                "                \"update\" : {\n" +
                "                    \"desc\" : \"更新时where条件所使用的字段\",\n" +
                "                    \"qualified_field\" : [ \n" +
                "                        \"LOAN_NO\"\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"select\" : {\n" +
                "                    \"sql\" : \"SELECT * FROM core_lm_loan WHERE LOAN_NO='${[LOAN_NO]}'\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"pre_processor\" : \"\",\n" +
                "            \"post_processor\" : \"\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"table\" : \"lm_loan\"\n" +
                "}", new TypeToken<Map<String, Object>>(){}.getType());
        System.out.println(GSON().toJson(map));

    }


}
