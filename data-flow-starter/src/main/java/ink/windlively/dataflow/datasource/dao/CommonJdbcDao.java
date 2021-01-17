package ink.windlively.dataflow.datasource.dao;


import ink.windlively.dataflow.util.GeneralTools;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;


import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public interface CommonJdbcDao {

    void setDataSource(Object dataSource);

    Object getDataSource();

    /**
     * 通用的查询
     *
     * @param sql        查询SQL
     * @param dataSource 数据源实例或数据源名称
     * @param resultType 返回类型
     * @return SQL执行结果
     */
    default Object select(@NonNull String sql, @Nullable Object dataSource, @NonNull String resultType) {
        throw new UnsupportedOperationException();
    }

    /**
     * 通用查询
     *
     * @param sql        查询SQL
     * @param resultType 返回值类型
     * @return SQL执行结果
     */
    default Object select(@NonNull String sql, @NonNull String resultType) {
        throw new UnsupportedOperationException();
    }

    /**
     * SQL查询, 返回结果列表
     *
     * @param sql SQL语句
     * @return 查询结果组成的list
     */
    @NonNull
    List<Map<String, Object>> select(@NonNull String sql);

    /**
     * SQL查询, 返回结果对象列表
     *
     * @param sql         SQL语句
     * @param clazz       映射的对象
     * @param nameMapping 是否自动映射数据库名与对象名
     * @param <R>         映射类型
     * @return 查询结果列表
     */
    @NonNull
    default <R> List<R> select(String sql, Class<R> clazz, boolean nameMapping) {
        return select(sql).stream()
                .map(m -> {
                    try {
                        R r = clazz.getConstructor().newInstance();
                        GeneralTools.setProperties(m, r, nameMapping);
                        return r;
                    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                        throw new IllegalArgumentException(e);
                    }
                }).collect(Collectors.toList());
    }

    /**
     * @see #select(String, Class, boolean)
     */
    @NonNull
    default <R> List<R> select(String sql, Class<R> clazz) {
        return select(sql, clazz, false);
    }

    /**
     * 查询一条结果
     *
     * @param sql SQL语句
     * @return 查询结果, 若无结果返回null
     */
    @Nullable
    Map<String, Object> selectOne(@NonNull String sql);

    /**
     * 查询一条结果
     *
     * @param sql         SQL语句
     * @param clazz       返回对象类型
     * @param nameMapping 是否自动映射数据库名与对象属性
     * @param <R>         返回类型
     * @return 查询结果, 若无结果返回null
     */
    @Nullable
    default <R> R selectOne(String sql, Class<R> clazz, boolean nameMapping) {
        Map<String, Object> map = selectOne(sql);
        if (map == null) return null;
        try {
            R r = clazz.getConstructor().newInstance();
            GeneralTools.setProperties(map, r, nameMapping);
            return r;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @see #selectOne(String, Class, boolean)
     */
    @Nullable
    default <R> R selectOne(String sql, Class<R> clazz) {
        return selectOne(sql, clazz, false);
    }

    int update(String sql);

    int delete(String sql);

    int insert(String sql);

}
