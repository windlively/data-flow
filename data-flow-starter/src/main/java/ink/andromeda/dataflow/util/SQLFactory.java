package ink.andromeda.dataflow.util;

import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

public interface SQLFactory {

    /**
     * 插入一条记录
     *
     * @param data         待插入的列数据
     * @param customFields 自定义字段
     * @param schema       库名
     * @param table        表名
     * @return 插入数量
     */
    default int insert(Map<String, Object> data, @Nullable Map<String, String> customFields, @Nullable String schema, String table) {
        return 0;
    }

    /**
     * 更新一条记录
     *
     * @param data         更新数据
     * @param customFields 自定义字段
     * @param queryFields  更新时的查询字段
     * @param schema       库名
     * @param table        表名
     * @return 更新数量
     */
    default int update(Map<String, Object> data, Map<String, String> customFields, List<String> queryFields, String schema, String table) {
        return 0;
    }

    default int delete(Map<String, Object> data, List<String> queryFields, String schema, String table) {
        return 0;
    }

}
