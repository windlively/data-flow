package ink.andromeda.dataflow.util;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 通过核心库表实体类生成SQL语句的工具类
 * NOTE: 1. 实体类名(大驼峰式)与表名(下划线式)完全对应
 * 2. 实体类字段(小驼峰式)名与数据库字段名(下划线式)完全对应
 */
@Slf4j
public class SqlGenerator {

    public static String genInsert(@NonNull Map<String, Object> data, @NonNull String tableName, boolean defaultId) {
        StringBuilder SQL = new StringBuilder();
        SQL.append("INSERT INTO ").append(tableName);
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        data.forEach((field, value) -> {
            columns.add(field);
            if ("id".equalsIgnoreCase(field) && defaultId) {
                values.add("DEFAULT");
                return;
            }
            if ("create_time".equalsIgnoreCase(field) || "update_time".equalsIgnoreCase(field)) {
                values.add("NOW()");
                return;
            }
            values.add(GeneralTools.javaValToSqlVal(value));
        });
        SQL.append("(");
        SQL.append(Joiner.on(", ").join(columns)).append(")");
        SQL.append(" VALUES (");
        SQL.append(Joiner.on(", ").join(values)).append(")");
        log.debug("generate insert SQL: [{}]", SQL.toString());
        return SQL.toString();
    }

    public static String genSelect(@NonNull Map<String, Object> data, @NonNull String tableName, boolean ignoreNullVal, String... qualifiedField) {
        /*
            生成SQL语句:
            SELECT * FROM ... WHERE ...
            WHERE条件的值从传入的coreBean中获取
         */
        StringBuilder SQL = new StringBuilder();
        SQL.append("SELECT * FROM ").append(tableName);
        if (qualifiedField != null) {
            SQL.append(" WHERE ");
            for (int i = 0; i < qualifiedField.length; i++) {
                String tableField = qualifiedField[i];
                Object value = data.get(tableField);
                if (value == null) {
                    if (!ignoreNullVal)
                        throw new NullPointerException(String.format("the entity: [%s], field [%s] is required but now is null! ", data, tableField));
                }
                String strVal = GeneralTools.javaValToSqlVal(value);
                SQL.append(i == 0 ? "" : " AND ").append(tableField).append("=").append(strVal);
            }
        }
        log.debug("generate select SQL: [{}]", SQL.toString());
        return SQL.toString();
    }

    public static String genUpdate(@NonNull Map<String, Object> data, @NonNull String tableName, @NonNull String... qualifiedField) {
        /*
            UPDATE ... SET ... WHERE ...
            表名为传入的类的类名转换为下划线所得, 不设置id字段以及传入实体类为null的字段
         */
        StringBuilder SQL = new StringBuilder();
        SQL.append("UPDATE ").append(tableName).append(" SET ");

        List<String> list = new ArrayList<>();
        data.forEach((field, value) -> {
            if (field.equalsIgnoreCase("id"))
                return;
            if (field.equalsIgnoreCase("update_time")) {
                list.add(field + "=NOW()");
                return;
            }
            if (value == null)
                return;
            String strVal = GeneralTools.javaValToSqlVal(value);
            list.add(field + "=" + strVal);
        });
        SQL.append(Joiner.on(", ").join(list));
        SQL.append(" WHERE ");
        list.clear();
        SQL.append(genWhereCase(data, qualifiedField));
        log.debug("generate update SQL: [{}]", SQL.toString());
        return SQL.toString();
    }

    public static String genDelete(@NonNull Map<String, Object> data, @NonNull String tableName, @NonNull String... qualifiedField){
        StringBuilder SQL = new StringBuilder("DELETE FROM " + tableName + " WHERE ");
        SQL.append(genWhereCase(data, qualifiedField));
        log.debug("generate delete SQL: [{}]",SQL.toString());
        return SQL.toString();
    }

    private static String genWhereCase(@NonNull Map<String, Object> data, String[] qualifiedField) {
        List<String> wheres = new ArrayList<>();
        for (String tableFieldName : qualifiedField) {
            Object value = data.get(tableFieldName);
            String strVal = GeneralTools.javaValToSqlVal(value);
            wheres.add(tableFieldName + "=" + strVal);
        }

        return Joiner.on(" AND ").join(wheres);
    }
}
