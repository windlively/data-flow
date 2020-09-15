package ink.andromeda.dataflow.util;


import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * JSON配置文件检查器
 */
@Slf4j
public class JSONValidator {

    // 模板文件
    @Setter
    private Map<String, Object> template;

    public JSONValidator(Map<String, Object> template) {
        this.template = template;
    }

    /**
     * JSON格式校验
     *
     * @param config 需要校验的JSON内容
     * @return 错误信息列表, key: 错误位置, value: 错误信息
     */
    public Map<String, String> validate(Map<String, Object> config) {
        // 使用LinkedHashMap, 保证错误信息的顺序
        Map<String, String> errorMap = new LinkedHashMap<>();
        validate(config, "", template, errorMap);
        return errorMap;
    }

    @SuppressWarnings("unchecked")
    private void validate(Map<String, Object> config, String keyPrefix, Map<String, Object> templateFields, @NonNull Map<String, String> errorInfo) {
        // 遍历模板配置, k为字段名, v为校验规则
        templateFields.forEach((k, v) -> {
            Map<String, Object> regular = (Map<String, Object>) v;
            // 获取待校验JSON对应字段的值
            Object configItem = config.get(k);
            // 当前校验的位置
            String currentKeyPrefix = StringUtils.isEmpty(keyPrefix) ? k : (keyPrefix + "->" + k);
            // 是否必须
            boolean required = (boolean) regular.getOrDefault("required", false);
            if (configItem == null) {
                if (required)
                    errorInfo.put(currentKeyPrefix, "is required");
                return;
            }
            // 字段值类型, 使用'|'描述多种允许类型
            String tType = (String) regular.get("type");
            String[] types = tType.split("\\|");
            Class<?> javaType = null;
            // 判断当前值是否符合规则
            for (String t : types) {
                Class<?> c = getType(t);
                if (c.isInstance(configItem)) {
                    javaType = c;
                    break;
                }
            }

            // 未找到相匹配的类型
            if (javaType == null) {
                errorInfo.put(currentKeyPrefix, String.format("value [%s] type is [%s], not belong [%s]", configItem, configItem.getClass().getSimpleName(), tType));
                return;
            }

            processStringValue(errorInfo, regular, configItem, currentKeyPrefix, javaType);

            Map<String, Object> fieldsRegular;

            if (javaType.equals(Map.class)
                && validateOptionalFields(regular, currentKeyPrefix, (Map<String, Object>) configItem, errorInfo)
                && (((fieldsRegular = regular.get("fields_ref") == null ?
                    (Map<String, Object>) regular.get("fields") : findObject(template, (String) regular.get("fields_ref"))) != null)
                    || regular.get("reg_key") != null)) {
                validateRegKey(errorInfo, regular, (Map<String, Object>) configItem, currentKeyPrefix, fieldsRegular);
                if (fieldsRegular != null)
                    validate((Map<String, Object>) configItem, currentKeyPrefix, fieldsRegular, errorInfo);
                return;
            }

            if (javaType.equals(List.class)) {
                Map<String, Object> lTemplate = (Map<String, Object>) regular.get("item");
                String lType = (String) lTemplate.get("type");
                String[] lTypes = lType.split("\\|");
                int index = 0;
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
                        errorInfo.put(lCurrentKeyPrefix, String.format("value [%s] type is [%s], not belong [%s]", i, i.getClass().getSimpleName(), lType));
                        continue;
                    }

                    processStringValue(errorInfo, lTemplate, i, lCurrentKeyPrefix, lJavaType);

                    Map<String, Object> lFieldsRegular;
                    if (lJavaType.equals(Map.class)
                        && validateOptionalFields(lTemplate, lCurrentKeyPrefix, (Map<String, Object>) i, errorInfo)
                        && (((lFieldsRegular = lTemplate.get("fields_ref") == null ? (Map<String, Object>) lTemplate.get("fields") : findObject(template, (String) lTemplate.get("fields_ref"))) != null)
                            || lTemplate.get("reg_key") != null)) {
                        validateRegKey(errorInfo, lTemplate, (Map<String, Object>) i, lCurrentKeyPrefix, lFieldsRegular);
                        if (lFieldsRegular != null)
                            validate((Map<String, Object>) i, lCurrentKeyPrefix, lFieldsRegular, errorInfo);
                        continue;
                    }
                    if (lJavaType.equals(List.class))
                        validate((Map<String, Object>) i, lCurrentKeyPrefix, (Map<String, Object>) lTemplate.get("item"), errorInfo);
                }
            }
        });
    }

    /**
     * String类型校验
     *
     * @param errorInfo        错误信息
     * @param desc             校验规则
     * @param configItem       待校验值
     * @param currentKeyPrefix 校验的位置
     * @param javaType         JAVA类类型
     */
    private static void processStringValue(@NonNull Map<String, String> errorInfo, Map<String, Object> desc, Object configItem, String currentKeyPrefix, Class<?> javaType) {
        if (javaType.equals(String.class)) {
            List<String> valueOption;
            String valueReg;
            //noinspection unchecked
            if ((valueOption = (List<String>) desc.get("value_option")) != null
                && !valueOption.contains((String) configItem)) {
                // 待校验的值不在'value_option(可选值)'之中
                errorInfo.put(currentKeyPrefix, String.format("optional value is %s, not contains [%s]", valueOption, configItem));
            } else if (StringUtils.isNotEmpty(valueReg = (String) desc.get("value_reg"))
                       && !((String) configItem).matches(valueReg)) {
                // 待校验的值不符合正则表达式规则
                errorInfo.put(currentKeyPrefix, String.format("value [%s] not match [%s]", configItem, valueReg));
            }
        }
    }


    @SuppressWarnings("unchecked")
    private void validateRegKey(@NonNull Map<String, String> errorInfo, Map<String, Object> curTemplate,
                                Map<String, Object> item, String currentKeyPrefix, @Nullable Map<String, Object> fieldsRegular) {
        if (curTemplate.get("reg_key") != null) {
            ((Map<String, Object>) curTemplate.get("reg_key")).forEach((regKey, v1) ->
                    item.keySet().stream().filter(ck -> (fieldsRegular == null || !fieldsRegular.containsKey(ck)) && ck.matches(regKey)).forEach(ck -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put(ck, v1);
                                validate(item, currentKeyPrefix, map, errorInfo);
                            }
                    ));
        }
    }

    private boolean validateOptionalFields(Map<String, Object> template, String keyPrefix, Map<String, Object> config, Map<String, String> errorInfo) {
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
                errorInfo.put(keyPrefix, String.format("value field must match at least one option in %s", template.get("fields_option")));
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

}
