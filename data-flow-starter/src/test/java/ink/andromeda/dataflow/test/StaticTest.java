package ink.andromeda.dataflow.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.reflect.TypeToken;
import ink.andromeda.dataflow.DataFlowAutoConfiguration;
import ink.andromeda.dataflow.core.ExpressionService;
import ink.andromeda.dataflow.core.SpringELExpressionService;
import ink.andromeda.dataflow.util.JSONValidator;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ink.andromeda.dataflow.util.GeneralTools.GSON;
import static ink.andromeda.dataflow.util.GeneralTools.toJSONString;

@SpringBootTest

@Slf4j
public class StaticTest {


    @Test
    public void gsonTest(){
        String json = "{\n" +
                      "    \"_id\" : 11,\n" +
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
                      "}";


        Map<String, Object> map = GSON().fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode node = objectMapper.readTree(json);
            System.out.println(node.toString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println(GSON().toJson(map));

    }

    @Test
    public void jsonValidatorTest() throws URISyntaxException, IOException {
        Map<String, Object> validateConfig = GSON().fromJson(String.join("",
                Files.readAllLines(Paths.get(this.getClass().getResource("/data-flow-config-example/validate-template-example.json").toURI()))),
                new TypeToken<Map<String, Object>>(){}.getType());

        List<Map<String, Object>> config = GSON().fromJson(String.join("",
                Files.readAllLines(Paths.get(this.getClass().getResource("/data-flow-config-example/sync-config-example-2.json").toURI()))),
                new TypeToken<List<Map<String, Object>>>(){}.getType());

        @SuppressWarnings("unchecked")
        JSONValidator validator = new JSONValidator((Map<String, Object>) validateConfig.get("template"));
        Map<String, String> errorInfo = validator.validate(config.get(0));

        log.info(toJSONString(errorInfo));

    }




}
