package ink.windlively.dataflow.core;

import org.springframework.lang.Nullable;

/**
 * 表达式解析服务
 *
 * @param <T> 表达式类型
 */
public interface ExpressionService<T> {

    /**
     * 执行表达式
     *
     * @param expression    表达式
     * @param additionalEnv 附加的环境参数, 可为null
     * @return 执行结果
     */
    @Nullable
    default Object executeExpression(T expression, @Nullable EnvironmentContext additionalEnv) {
        return executeExpression(expression, additionalEnv, Object.class);
    }

    /**
     * 执行表达式, 返回指定类型的结果
     *
     * @param expression    表达式
     * @param additionalEnv 附加的环境参数, 可为null
     * @param clazz         返回类型的class
     * @param <R>           返回类型
     * @return 执行结果
     */
    @Nullable
    <R> R executeExpression(T expression, @Nullable EnvironmentContext additionalEnv, Class<R> clazz);

    /**
     * 执行表达式
     *
     * @param expression 表达式
     * @return 执行结果
     */
    @Nullable
    default Object executeExpression(T expression) {
        return executeExpression(expression, (EnvironmentContext) null);
    }

    /**
     * 执行表达式
     *
     * @param expression 表达式
     * @param clazz      返回类型class
     * @param <R>        返回类型
     * @return 执行结果
     */
    @Nullable
    default <R> R executeExpression(T expression, Class<R> clazz) {
        return executeExpression(expression, null, clazz);
    }
}
