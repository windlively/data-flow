package ink.andromeda.dataflow.util;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static ink.andromeda.dataflow.util.GeneralTools.javaValToSqlVal;

public class SQLFactory {

    /**
     * 插入一条记录
     *
     * @param data         待插入的列数据
     * @param customFields 自定义字段值, 例如 'NOW(), DEFAULT' 等静态值
     * @param schema       库名
     * @param table        表名
     * @return 插入数量
     */
    public static String insert(Map<String, Object> data, @Nullable Map<String, String> customFields, @Nullable String schema, String table) {
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
            if (value == null) {
                continue;
            }
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
     * 更新一条记录
     *
     * @param data         更新数据
     * @param customFields 自定义字段
     * @param matchFields  更新时的查询字段
     * @param schema       库名
     * @param table        表名
     * @return 更新数量
     */
    public static int update(Map<String, Object> data, Map<String, String> customFields, List<String> matchFields, String schema, String table) {
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
            if (value == null) {
                continue;
            }
            columnName.add(field);
            columnValue.add(customValue != null ?
                    customValue : javaValToSqlVal(value));
        }
        SQL.append("(").append(String.join(",", columnName)).append(")");
        SQL.append(" VALUES ");
        SQL.append("(").append(String.join(",", columnValue)).append(")");
        return 0;
    }

    public static int delete(Map<String, Object> data, List<String> matchFields, String schema, String table) {
        return 0;
    }

    public static String select(Map<String, Object> data, @Nullable List<String> selectFields, List<String> matchFields, String schema, String table) {

        Assert.isTrue(data != null && !data.isEmpty(), "data is null, could not generate select sql");

        Assert.isTrue(!CollectionUtils.isEmpty(matchFields), "match fields is null");

        StringBuilder SQL = new StringBuilder("SELECT ");

        if (!CollectionUtils.isEmpty(selectFields)) {
            SQL.append(String.join(",", selectFields));
        } else {
            SQL.append("*");
        }

        SQL.append(" FROM ").append(schema).append('.').append(table);

        SQL.append(" WHERE ");

        SQL.append(matchFields.stream()
                .map(f -> f + "=" + javaValToSqlVal(
                        Objects.requireNonNull(data.get(f), "field " + f + " value is null"))
                ).collect(Collectors.joining(" AND ")));

        return SQL.toString();
    }

}
