package ink.andromeda.dataflow.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.stream.Stream;

/**
 * JSON Object数据检查器<br>
 */
@Slf4j
public class JSONValidator {

    // 模板(规则)
    private Map<String, Object> template;

    private static final String WRAPPER_FIELD_NAME = "root";

    public void setTemplate(Map<String, Object> template) {
        Map<String, Object> map = new HashMap<>(1);
        // 将校验规则外包一层
        map.put(WRAPPER_FIELD_NAME, template);
        this.template = map;
    }

    public JSONValidator(Map<String, Object> template) {
        setTemplate(template);
    }

    /**
     * JSON Object格式校验
     *
     * @param config 待校验的JSON数据
     * @return 错误信息列表, key: 错误位置, value: 错误信息
     */
    public LinkedHashMap<String, String> validate(Map<String, Object> config) {
        return wrapperAndValidate(config);
    }

    /**
     * JSON Array格式校验
     *
     * @param config 待校验的JSON数据
     * @return 错误信息列表, key: 错误位置, value: 错误信息
     */
    public LinkedHashMap<String, String> validate(List<Object> config) {
        return wrapperAndValidate(config);
    }

    /**
     * 包装并校验JSON数据
     *
     * @param config 原待校验数据
     * @return 错误信息
     */
    private LinkedHashMap<String, String> wrapperAndValidate(Object config) {
        // 使用LinkedHashMap, 保证错误信息的顺序
        LinkedHashMap<String, String> errorInfo = new LinkedHashMap<>();
        Map<String, Object> wrapper = new HashMap<>(1);
        // 将校验数据外包一层
        wrapper.put(WRAPPER_FIELD_NAME, config);
        validate(wrapper, "", template, errorInfo);
        return errorInfo;
    }

    /**
     * 逐个字段校验
     *
     * @param config           待校验数据
     * @param keyPrefix        当前校验的位置
     * @param validationFields 校验的字段及其规则
     * @param errorInfo        错误信息
     */
    @SuppressWarnings("unchecked")
    private void validate(Map<String, Object> config, String keyPrefix, Map<String, Object> validationFields, @NonNull Map<String, String> errorInfo) {
        // 遍历模板配置, k为字段名, v为校验规则
        validationFields.forEach((k, v) -> {
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

            // 字符串类型值校验
            validateStringType(regular, configItem, currentKeyPrefix, javaType, errorInfo);

            // object类型值校验
            validateObjectType(currentKeyPrefix, javaType, regular, configItem, errorInfo);

            // list类型值校验
            if (javaType.equals(List.class)) {
                // item为list中每一项数据的格式
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

                    validateStringType(lTemplate, i, lCurrentKeyPrefix, lJavaType, errorInfo);

                    validateObjectType(lCurrentKeyPrefix, lJavaType, lTemplate, i, errorInfo);

                    if (lJavaType.equals(List.class))
                        validate((Map<String, Object>) i, lCurrentKeyPrefix, (Map<String, Object>) lTemplate.get("item"), errorInfo);
                }
            }
        });
    }

    /**
     * JSON Object类型数据的校验
     *
     * @param prefixKey  校验的当前位置
     * @param javaType   数据的Java Class类型
     * @param regular    该数据项的校验规则
     * @param configItem 待校验数据项
     * @param errorInfo  错误信息
     */
    @SuppressWarnings("unchecked")
    private void validateObjectType(String prefixKey, Class<?> javaType, Map<String, Object> regular, Object configItem, Map<String, String> errorInfo) {
        Map<String, Object> fieldsRegular;
        if (javaType.equals(Map.class)
                && validateOptionalFields(regular, prefixKey, (Map<String, Object>) configItem, errorInfo)
                && (((fieldsRegular = regular.get("fields_ref") == null ?
                (Map<String, Object>) regular.get("fields") : findObject(template, (String) regular.get("fields_ref"))) != null)
                || regular.get("reg_key") != null)) {
            validateRegKey(errorInfo, regular, (Map<String, Object>) configItem, prefixKey, fieldsRegular);
            if (fieldsRegular != null)
                validate((Map<String, Object>) configItem, prefixKey, fieldsRegular, errorInfo);
        }
    }

    /**
     * String类型校验
     *
     * @param errorInfo        错误信息
     * @param regular          校验规则
     * @param configItem       待校验值
     * @param currentKeyPrefix 校验的位置
     * @param javaType         JAVA类类型
     */
    private void validateStringType(Map<String, Object> regular, Object configItem, String currentKeyPrefix, Class<?> javaType, @NonNull Map<String, String> errorInfo) {
        if (javaType.equals(String.class)) {
            List<String> valueOption;
            String valueReg;
            //noinspection unchecked
            if ((valueOption = (List<String>) regular.get("value_option")) != null
                    && !valueOption.contains((String) configItem)) {
                // 待校验的值不在'value_option(可选值)'之中
                errorInfo.put(currentKeyPrefix, String.format("optional value is %s, not contains [%s]", valueOption, configItem));
            } else if (StringUtils.isNotEmpty(valueReg = (String) regular.get("value_reg"))
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
                errorInfo.put(keyPrefix, String.format("value field must match at least one option in %s", Arrays.toString(fieldsOption)));
                return false;
            }
        }
        return true;
    }

    private static Map<String, Object> findObject(Map<String, Object> object, String path) {
        String[] keys = path.split("\\.");
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) object.get(WRAPPER_FIELD_NAME);
        for (String key : keys) {
            //noinspection unchecked
            result = (Map<String, Object>) result.get(key);
        }
        return result;
    }

    /**
     * 获取规则文件中[type]字段对应的Java类型
     *
     * @param type 值类型
     * @return JAVA Class类型
     */
    @NonNull
    private static Class<?> getType(String type) {
        Objects.requireNonNull(type);
        type = type.toLowerCase();
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
                log.error("unknown value type: '{}'", type);
                throw new IllegalArgumentException("unknown value type: " + type);
        }
    }

}
