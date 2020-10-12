package ink.andromeda.dataflow.util;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SQLFactory {

    /**
     * 插入一条记录
     *
     * @param data         待插入的列数据
     * @param customFields 自定义字段
     * @param schema       库名
     * @param table        表名
     * @return 插入数量
     */
    public static int insert(Map<String, Object> data, @Nullable Map<String, String> customFields, @Nullable String schema, String table) {
        return 0;
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
        return 0;
    }

    public static int delete(Map<String, Object> data, List<String> matchFields, String schema, String table) {
        return 0;
    }

    public static int select(Map<String ,Object> data, @Nullable List<String> selectFields, List<String> matchFields, String schema, String table){

        Assert.isTrue(data != null && !data.isEmpty(), "");





        return 0;
    };

}
