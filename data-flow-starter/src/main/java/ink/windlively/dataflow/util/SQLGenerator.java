package ink.windlively.dataflow.util;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static ink.windlively.dataflow.util.GeneralTools.javaValToSqlVal;

/**
 * 根据提供的map集合生成SQL语句
 */
public class SQLGenerator {

    /**
     * 生成插入的SQL语句
     *
     * @param data         提供的数据
     * @param customFields 自定义字段, 例如: NOW(), DEFAULT 等静态值
     * @param schema       库名
     * @param table        表名
     */
    public static String genInsertSQL(Map<String, Object> data, @Nullable Map<String, String> customFields, String schema, String table) {
        Assert.isTrue(data != null && !data.isEmpty(), "data is null, could not generate insert sql");
        customFields = customFields == null ? Collections.emptyMap() : customFields;
        // 自定义字段的值将覆盖传入的data map中的值
        data.putAll(customFields);
        StringBuilder SQL = new StringBuilder("INSERT INTO ").append(schema).append('.').append(table).append(" ");
        List<String> columnName = new ArrayList<>(data.size());
        List<String> columnValue = new ArrayList<>(data.size());
        for (Map.Entry<String, Object> e : data.entrySet()) {
            String field = e.getKey();
            Object value = e.getValue();
            String customValue = customFields.get(field);
            if (value == null) continue;
            columnName.add(field);
            columnValue.add(customValue != null ?
                    customValue : javaValToSqlVal(value));
        }
        SQL.append("(").append(String.join(",", columnName)).append(")");
        SQL.append(" VALUES ");
        SQL.append("(").append(String.join(",", columnValue)).append(")");
        return SQL.toString();
    }

    /**
     * 生成更新SQL语句
     *
     * @param data         数据
     * @param customFields 自定义字段
     * @param matchFields  where条件所要匹配的字段
     * @param schema       库名
     * @param table        表名
     */
    public static String genUpdateSQL(Map<String, Object> data, Map<String, String> customFields, List<String> matchFields, String schema, String table) {
        Assert.isTrue(data != null && !data.isEmpty(), "data is null, could not generate insert sql");
        Assert.isTrue(!CollectionUtils.isEmpty(matchFields), "match fields is null");
        customFields = customFields == null ? Collections.emptyMap() : customFields;
        data.putAll(customFields);
        StringBuilder SQL = new StringBuilder("UPDATE ").append(schema).append('.').append(table)
                .append(" SET ");
        List<String> setItem = new ArrayList<>(data.size());
        for (Map.Entry<String, Object> e : data.entrySet()) {
            String field = e.getKey();
            Object value = e.getValue();
            String customValue = customFields.get(field);
            if (value == null) continue;
            setItem.add(field + "=" + (customValue == null ? javaValToSqlVal(value) : value));
        }
        SQL.append(String.join(",", setItem));
        SQL.append(" WHERE ");
        SQL.append(genWhereCondition(matchFields, data));
        return SQL.toString();
    }

    /**
     * 生成删除SQL语句
     *
     * @param data        数据
     * @param matchFields 匹配字段
     * @param schema      库名
     * @param table       表名
     */
    public static String genDeleteSQL(Map<String, Object> data, List<String> matchFields, String schema, String table) {
        Assert.isTrue(data != null && !data.isEmpty(), "data is null, could not generate select sql");
        Assert.isTrue(!CollectionUtils.isEmpty(matchFields), "match fields is null");
        return "DELETE FROM " + schema + '.' + table + " WHERE " +
                genWhereCondition(matchFields, data);
    }

    /**
     * 生成查询的SQL语句
     *
     * @param data         提供的数据
     * @param selectFields 查询字段, 可为null
     * @param matchFields  匹配字段
     * @param schema       库名
     * @param table        表名
     */
    public static String genSelectSQL(Map<String, Object> data, @Nullable List<String> selectFields, List<String> matchFields, String schema, String table) {
        Assert.isTrue(data != null && !data.isEmpty(), "data is null, could not generate select sql");
        Assert.isTrue(!CollectionUtils.isEmpty(matchFields), "match fields is null");
        StringBuilder SQL = new StringBuilder("SELECT ");
        SQL.append(!CollectionUtils.isEmpty(selectFields) ?
                String.join(",", selectFields) : "*");
        SQL.append(" FROM ").append(schema).append('.').append(table);
        SQL.append(" WHERE ");
        SQL.append(genWhereCondition(matchFields, data));
        return SQL.toString();
    }

    private static String genWhereCondition(List<String> matchFields, Map<String, Object> data) {
        return matchFields.stream()
                .map(f -> f + "=" + javaValToSqlVal(
                        Objects.requireNonNull(data.get(f), "field " + f + " in where case value is null"))
                ).collect(Collectors.joining(" AND "));
    }

}
