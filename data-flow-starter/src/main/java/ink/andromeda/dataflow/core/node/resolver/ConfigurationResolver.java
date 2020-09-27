package ink.andromeda.dataflow.core.node.resolver;

import ink.andromeda.dataflow.core.EnvironmentContext;
import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * 配置解析器
 *
 * @param <R> 该条配置的返回值类型
 */
public interface ConfigurationResolver<R> {

    /**
     * 解析器名称, 与JSON配置中的key名一致
     *
     * @return resolver name
     */
    String getName();

    void setEnvironmentContext(EnvironmentContext environmentContext);

    @Nullable
    EnvironmentContext getEnvironmentContext();

    /**
     * 根据环境变量执行解析
     *
     * @param env 解析环境, 提供解析时所必要的参数
     * @return 配置执行完毕的返回值
     * @throws Exception 解析发生异常
     */
    R resolve(EnvironmentContext env) throws Exception;

    /**
     * @return 解析器的描述
     */
    default String getDescription() {
        return "this resolver has no description";
    }

    default R resolve() throws Exception {
        return resolve(Objects.requireNonNull(getEnvironmentContext(), "environment context is null"));
    }
}
