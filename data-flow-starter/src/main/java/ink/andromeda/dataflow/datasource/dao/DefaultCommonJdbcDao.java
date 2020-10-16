package ink.andromeda.dataflow.datasource.dao;

import ink.andromeda.dataflow.datasource.DynamicDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

import static ink.andromeda.dataflow.util.GeneralTools.*;
import static org.springframework.util.ReflectionUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

@Slf4j
public class DefaultCommonJdbcDao implements CommonJdbcDao {

    private final DynamicDataSource dynamicDataSource;

    public DefaultCommonJdbcDao(DynamicDataSource dynamicDataSource) {
        this.dynamicDataSource = dynamicDataSource;
    }

    @Override
    public void setDataSource(Object dataSource) {
        Assert.isTrue(dataSource instanceof String, "please provide data source name");
        dynamicDataSource.changeLookupKey((String) dataSource);
    }

    @Override
    public DataSource getDataSource() {
        return dynamicDataSource;
    }

    private DataSource getNonNullDataSource() {
        return Objects.requireNonNull(getDataSource(), "data source is null");
    }

    @Override
    @NonNull
    public List<Map<String, Object>> select(@NonNull String sql) {
        try (
                Connection connection = dynamicDataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
        ) {
            List<Map<String, Object>> list = new ArrayList<>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            if (resultSet.next()) {
                Map<String, Object> object = new HashMap<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    object.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                }
                list.add(object);
            }
            return list;
        } catch (SQLException ex) {
            throw new IllegalStateException("exception in execute sql [" + sql + "]", ex);
        }
    }

    @Override
    @NonNull
    public <R> List<R> select(String sql, Class<R> clazz, boolean nameMapping) {
        try (
                Connection connection = dynamicDataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
        ) {
            List<R> list = new ArrayList<>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            Map<Integer, Method> methodCache = new HashMap<>(metaData.getColumnCount());
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String fieldName = metaData.getColumnLabel(i);
                if (nameMapping) fieldName = upCaseToCamelCase(fieldName, false);
                String methodName = genSetterMethodName(fieldName);
                Method method = Objects.requireNonNull(
                        findMethod(clazz, methodName, (Class<?>) null),
                        "class " + clazz.getName() + " could not found method " + methodName
                );
                methodCache.put(i, method);
            }
            Constructor<R> constructor = clazz.getConstructor();
            if (resultSet.next()) {
                R r = constructor.newInstance();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    Method method = methodCache.get(i);
                    invokeMethod(method, r, conversionService().convert(resultSet.getObject(i), method.getParameterTypes()[0]));
                }
                list.add(r);
            }
            return list;
        } catch (SQLException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException("exception in execute sql [" + sql + "], message: " + ex.getMessage(), ex);
        }
    }

    @NonNull
    @Override
    public <R> List<R> select(String sql, Class<R> clazz) {
        return select(sql, clazz, false);
    }

    @Override
    public Map<String, Object> selectOne(@NonNull String sql) {
        List<Map<String, Object>> list = select(sql);
        if (list.isEmpty()) return null;
        if (list.size() > 1) {
            throw new IllegalStateException("exception in execute sql [" + sql + "], except 1 result, but found " + list.size());
        }
        return list.get(0);
    }

    @Override
    public <R> R selectOne(String sql, Class<R> clazz, boolean nameMapping) {
        List<R> list = select(sql, clazz);
        if (list.isEmpty()) return null;
        if (list.size() > 1) {
            throw new IllegalStateException("exception in execute sql [" + sql + "], except 1 result, but found " + list.size());
        }
        return list.get(0);
    }

    @Override
    public <R> R selectOne(String sql, Class<R> clazz) {
        return selectOne(sql, clazz, false);
    }

    @Override
    public int update(String sql) {
        DataSource dataSource = getDataSource();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            return statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("exception in execute sql [" + sql + "], message: " + ex.getMessage(), ex);
        }
    }

    @Override
    public int delete(String sql) {
        return update(sql);
    }

    @Override
    public int insert(String sql) {
        return update(sql);
    }

    @Override
    public Object select(@NonNull String sql, @Nullable Object dataSource, @NonNull String resultType) {
        if(StringUtils.isBlank((CharSequence) dataSource))
            return select(sql, resultType);
        try {
            Assert.isTrue(dataSource instanceof String, "param 'dataSource' must be string");
            dynamicDataSource.changeLookupKey((String) dataSource);
            return select(sql, resultType);
        } finally {
            dynamicDataSource.resetToDefault();
        }
    }

    @Override
    public Object select(@NonNull String sql, @NonNull String resultType) {
        switch (resultType) {
            case "list":
                return select(sql);
            case "map":
            case "object":
                return selectOne(sql);
        }
        throw new IllegalArgumentException("not support result type: " + resultType);
    }

}
