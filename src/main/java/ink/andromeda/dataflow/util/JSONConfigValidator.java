package ink.andromeda.dataflow.util;


import com.google.gson.reflect.TypeToken;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

import static ink.andromeda.dataflow.util.GeneralUtils.GSON;

/**
 * JSON配置文件检查器
 */
@Slf4j
public class JSONConfigValidator {

    // 模板文件
    @Setter
    private Map<String, Object> template;

    public JSONConfigValidator(Map<String, Object> template) {
        this.template = template;
    }

    public List<String> validate(Map<String, Object> config) {
        List<String> list = new ArrayList<>();
        validate(config, "", template, list);
        return list;
    }

    private void validate(Map<String, Object> config, String keyPrefix, Map<String, Object> templateFields, @NonNull List<String> errorList) {
        templateFields.forEach((k, v) -> {
            Map<String, Object> desc = (Map<String, Object>) v;
            Object configItem = config.get(k);
            String currentKeyPrefix = StringUtils.isEmpty(keyPrefix) ? k : (keyPrefix + "->" + k);
            boolean required = (boolean) desc.getOrDefault("required", false);
            if (configItem == null) {
                if (required)
                    errorList.add(String.format("'%s' is required!", currentKeyPrefix));
                return;
            }
            String tType = (String) desc.get("type");
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
            Map<String, Object> fieldsRegular;
            if (javaType.equals(Map.class)
                && validateOptionalFields(desc, currentKeyPrefix, (Map<String, Object>) configItem, errorList)
                && (((fieldsRegular = desc.get("fields_ref") == null ? (Map<String, Object>) desc.get("fields") : findObject(template, (String) desc.get("fields_ref"))) != null)
                    || desc.get("reg_key") != null)) {
                validateRegKey(errorList, desc, (Map<String, Object>) configItem, currentKeyPrefix, fieldsRegular);
                if (fieldsRegular != null)
                    validate((Map<String, Object>) configItem, currentKeyPrefix, fieldsRegular, errorList);
                return;
            }
            if (javaType.equals(List.class)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> lTemplate = (Map<String, Object>) desc.get("item");
                String lType = (String) lTemplate.get("type");
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
                    Map<String, Object> lFieldsRegular;
                    if (lJavaType.equals(Map.class)
                        && validateOptionalFields(lTemplate, lCurrentKeyPrefix, (Map<String, Object>) i, errorList)
                        && (((lFieldsRegular = lTemplate.get("fields_ref") == null ? (Map<String, Object>) lTemplate.get("fields") : findObject(template, (String) lTemplate.get("fields_ref"))) != null)
                            || lTemplate.get("reg_key") != null)) {
                        validateRegKey(errorList, lTemplate, (Map<String, Object>) i, lCurrentKeyPrefix, lFieldsRegular);
                        if (lFieldsRegular != null)
                            validate((Map<String, Object>) i, lCurrentKeyPrefix, lFieldsRegular, errorList);
                        continue;
                    }
                    if (lJavaType.equals(List.class))
                        validate((Map<String, Object>) i, lCurrentKeyPrefix, (Map<String, Object>) lTemplate.get("item"), errorList);
                }
            }
        });
    }

    private void validateRegKey(@NonNull List<String> errorList, Map<String, Object> curTemplate, Map<String, Object> item, String currentKeyPrefix, @Nullable Map<String, Object> fieldsRegular) {
        if (curTemplate.get("reg_key") != null) {
            //noinspection unchecked
            ((Map<String, Object>) curTemplate.get("reg_key")).forEach((regKey, v1) ->
                    item.keySet().stream().filter(ck -> (fieldsRegular == null || !fieldsRegular.containsKey(ck)) && ck.matches(regKey)).forEach(ck -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put(ck, v1);
                                validate(item, currentKeyPrefix, map, errorList);
                            }
                    ));
        }
    }

    private boolean validateOptionalFields(Map<String, Object> template, String keyPrefix, Map<String, Object> config, List<String> errorList) {
        if (template.get("fields_option") != null) {
            String[] fieldsOption = ((String) template.get("fields_option")).split("\\|");
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
                errorList.add(String.format("'%s' value field must match at least one option in [%s]", keyPrefix, template.get("fields_option")));
                return false;
            }
        }
        return true;
    }

    private static Map<String, Object> findObject(Map<String, Object> object, String path) {
        String[] keys = path.split("\\.");
        Map<String, Object> result = object;
        for (String key : keys) {
            //noinspection unchecked
            result = (Map<String, Object>) result.get(key);
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
            return GSON().fromJson(stringBuilder.toString(), new TypeToken<Map<String, Object>>() {
            }.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
