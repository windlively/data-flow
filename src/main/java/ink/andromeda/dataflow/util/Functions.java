package ink.andromeda.dataflow.util;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 为SpringEL提供的函数
 */
public class Functions {

    public static BigDecimal toDecimal(String string) {
        return new BigDecimal(string);
    }

    /**
     * 判断两个JSON Object中值不一样的字段是否与给定的变化字段一致
     * 当且仅当有变化的字段与changedFields给定的一致时返回true
     * @param m1
     * @param m2
     * @param changedFields 变化的字段
     */
    public static boolean matchFieldChange(Map<String, Object> m1, Map<String, Object> m2, String... changedFields) {
        List<String> different = findFieldChange(m1,m2);
        boolean match = true;
        List<String> fields = Arrays.asList(changedFields);
        for (String f: different) {
            if(!fields.contains(f)){
                match = false;
                break;
            }
        }
        return match;
        // return different.size() == changedFields.length && Stream.of(changedFields).allMatch(different::contains);
    }

    // 找出两个对象中值不一致的字段
    public static List<String> findFieldChange(Map<String, Object> m1, Map<String, Object> m2){
        return m1.entrySet()
                .stream()
                .filter(s -> !Objects.equals(s.getValue(), m2.get(s.getKey())))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
//        Stream.of(Functions.class.getMethods()).filter(m -> Modifier.isStatic(m.getModifiers())).forEach(m -> System.out.println(m.getName()));
    }
}
