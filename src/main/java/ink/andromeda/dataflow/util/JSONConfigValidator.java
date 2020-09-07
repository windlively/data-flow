package ink.andromeda.dataflow.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * JSON配置文件检查器
 * 根据模板文件检查配置, 如果 {@link #validate(JSONObject)} 返回集合为空则代表配置正确, 否则返回错误信息
 */
@Slf4j
public class JSONConfigValidator {

    // 模板文件
    @Setter
    private JSONObject template;

    public JSONConfigValidator(JSONObject template) {
        this.template = template;
    }

    public List<String> validate(JSONObject config) {
        List<String> list = new ArrayList<>();
        config = JSON.parseObject(config.toJSONString());
        validate(config, "", template, list);
        return list;
    }

    private void validate(JSONObject config, String keyPrefix, JSONObject templateFields, @NonNull List<String> errorList) {
        templateFields.forEach((k, v) -> {
            JSONObject desc = (JSONObject) JSON.toJSON(v);
            Object configItem = config.get(k);
            String currentKeyPrefix = StringUtils.isEmpty(keyPrefix) ? k : (keyPrefix + "->" + k);
            boolean required = desc.getBooleanValue("required");
            if (configItem == null) {
                if (required)
                    errorList.add(String.format("'%s' is required!", currentKeyPrefix));
                return;
            }
            String tType = desc.getString("type");
            String[] types = tType.split("\\|");
            Class<?> javaType = null;
            for (String t : types) {
                Class<?> c = getType(t);
                if (c.isInstance(configItem)) {
                    javaType = c;
                    break;
                }
            }

            if (javaType == null) {
                errorList.add(String.format("'%s' value '%s' type is '%s', not belong [%s]", currentKeyPrefix, configItem, configItem.getClass().getSimpleName(), tType));
                return;
            }
            JSONObject fieldsRegular;
            if (javaType.equals(Map.class)
                && validateOptionalFields(desc, currentKeyPrefix, (JSONObject) configItem, errorList)
                && (((fieldsRegular = desc.get("fields_ref") == null ? desc.getJSONObject("fields") : findObject(template, desc.getString("fields_ref"))) != null)
                    || desc.get("reg_key") != null)) {
                validateRegKey(errorList, desc, (JSONObject) configItem, currentKeyPrefix, fieldsRegular);
                if (fieldsRegular != null)
                    validate((JSONObject) configItem, currentKeyPrefix, fieldsRegular, errorList);
                return;
            }
            if (javaType.equals(List.class)) {
                JSONObject lTemplate = desc.getJSONObject("item");
                String lType = lTemplate.getString("type");
                String[] lTypes = lType.split("\\|");
                int index = 0;
                //noinspection unchecked
                for (Object i : ((List<Object>) configItem)) {
                    Class<?> lJavaType = null;
                    for (String t : lTypes) {
                        Class<?> c = getType(t);
                        if (c.isInstance(i)) {
                            lJavaType = c;
                            break;
                        }
                    }
                    String lCurrentKeyPrefix = currentKeyPrefix + "[" + index++ + "]";

                    if (lJavaType == null) {
                        errorList.add(String.format("'%s' value '%s' type is '%s', not belong [%s]", lCurrentKeyPrefix, i, i.getClass().getSimpleName(), lType));
                        continue;
                    }
                    JSONObject lFieldsRegular;
                    if (lJavaType.equals(Map.class)
                        && validateOptionalFields(lTemplate, lCurrentKeyPrefix, (JSONObject) i, errorList)
                        && (((lFieldsRegular = lTemplate.get("fields_ref") == null ? lTemplate.getJSONObject("fields") : findObject(template, lTemplate.getString("fields_ref"))) != null)
                            || lTemplate.get("reg_key") != null)) {
                        validateRegKey(errorList, lTemplate, (JSONObject) i, lCurrentKeyPrefix, lFieldsRegular);
                        if (lFieldsRegular != null)
                            validate((JSONObject) JSON.toJSON(i), lCurrentKeyPrefix, lFieldsRegular, errorList);
                        continue;
                    }
                    if (lJavaType.equals(List.class))
                        validate((JSONObject) JSON.toJSON(i), lCurrentKeyPrefix, lTemplate.getJSONObject("item"), errorList);
                }
            }
        });
    }

    private void validateRegKey(@NonNull List<String> errorList, JSONObject curTemplate, JSONObject item, String currentKeyPrefix, @Nullable JSONObject fieldsRegular) {
        if (curTemplate.get("reg_key") != null) {
            curTemplate.getJSONObject("reg_key").forEach((regKey, v1) ->
                    item.keySet().stream().filter(ck -> (fieldsRegular == null || !fieldsRegular.containsKey(ck)) && ck.matches(regKey)).forEach(ck ->
                            validate(item, currentKeyPrefix, new JSONObject().fluentPut(ck, v1), errorList)));
        }
    }

    private boolean validateOptionalFields(JSONObject template, String keyPrefix, JSONObject config, List<String> errorList) {
        if (template.getString("fields_option") != null) {
            String[] fieldsOption = template.getString("fields_option").split("\\|");
            boolean flag = false;
            for (String option : fieldsOption) {
                String[] fs = option.split(",");
                //noinspection rawtypes
                if (Stream.of(fs).allMatch(((Map) config)::containsKey)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                errorList.add(String.format("'%s' value field must match at least one option in [%s]", keyPrefix, template.getString("fields_option")));
                return false;
            }
        }
        return true;
    }

    private static JSONObject findObject(JSONObject object, String path) {
        String[] keys = path.split("\\.");
        JSONObject result = object;
        for (String key : keys) {
            result = result.getJSONObject(key);
        }
        return result;
    }

    @NonNull
    private static Class<?> getType(String type) {
        switch (type) {
            case "string":
            case "String":
                return String.class;
            case "number":
                return Number.class;
            case "map":
            case "object":
                return Map.class;
            case "boolean":
                return Boolean.class;
            case "list":
                return List.class;
            default:
                log.error("unknown type: {}", type);
                throw new IllegalArgumentException("unknown type: " + type);
        }
    }


    public static void main(String[] args) {
        JSONObject config = (JSONObject) ((JSONArray) readFile("数据同步.json")).get(0);
        JSONObject template = (JSONObject) readFile("sync_config_template.json");
        JSONConfigValidator checker = new JSONConfigValidator(template);

        System.out.println(checker.validate(config));
    }

    private static Object readFile(String path) {
        try (
                InputStream inputStream = JSONConfigValidator.class.getResourceAsStream("/business-example-config/" + path)
        ) {
            Scanner scanner = new Scanner(inputStream);
            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine());
            }
            return JSON.parse(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
