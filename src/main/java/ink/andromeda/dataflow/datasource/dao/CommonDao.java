package ink.andromeda.dataflow.datasource.dao;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import net.abakus.coresystem.dynamicdatasource.DynamicDataSource;
import net.abakus.coresystem.util.CommonUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.util.ReflectionUtils.*;

@Repository
@Slf4j
public class CommonDao {

    private final DynamicDataSource dynamicDataSource;

    public CommonDao(DynamicDataSource dynamicDataSource) {
        this.dynamicDataSource = dynamicDataSource;
    }

    /**
     * 全局SQL查询, 支持项目中的所有数据源
     *
     * @param sql            SQL语句
     * @param dataSourceName 数据源名称
     * @param resultType     返回类型: map|list
     * @return map -> JSONObject, list -> List<JSONObject>
     */
    public Object select(@NonNull String sql, @NonNull String dataSourceName, @NonNull String resultType) {
        long t = System.currentTimeMillis();
        DataSource dataSource = Objects.requireNonNull(dynamicDataSource.getDataSource(dataSourceName), "could not found data source " + dataSourceName);
        try (
                // 这三项需要释放资源
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            log.info("sql: [{}] 耗时: {}ms", sql, System.currentTimeMillis() - t);
            ResultSetMetaData metaData = resultSet.getMetaData();
            switch (resultType) {
                case "map": {
                    Map<String, Object> result = new JSONObject();
                    if (resultSet.next()) {
                        for (int i = 1; i <= metaData.getColumnCount(); i++)
                            result.put(metaData.getColumnName(i), resultSet.getObject(i));
                    } else {
                        result = null;
                    }
                    if (resultSet.next())
                        throw new RuntimeException("result type is map, but found multi result!");
                    return result;
                }
                case "list": {
                    List<JSONObject> result = new ArrayList<>();
                    while (resultSet.next()) {
                        JSONObject item = new JSONObject();
                        for (int i = 1; i <= metaData.getColumnCount(); i++)
                            item.put(metaData.getColumnName(i), resultSet.getObject(i));
                        result.add(item);
                    }
                    return result;
                }
                default:
                    throw new Exception(String.format("unknown type: %s, just support types: %s, %s", resultType, "map", "list"));
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("exception in SQL: [%s], message: %s", sql, e.toString()), e);
        }
    }


    private static void close(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ex) {
            log.error("资源释放异常", ex);
        }
    }


    public static <T> T resultSetToCoreBean(ResultSet resultSet, ResultSetMetaData metaData, Class<T> clazz) throws SQLException {
        T result;
        try {
            result = clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnName(i);
            String fieldName = CommonUtils.upCaseToCamelCase(columnName, false);
            Field field = findField(clazz, fieldName);
            assert field != null;
            makeAccessible(field);
            setField(field, result, CommonUtils.conversionService().convert(resultSet.getObject(i), field.getType()));
        }
        return result;
    }
}
