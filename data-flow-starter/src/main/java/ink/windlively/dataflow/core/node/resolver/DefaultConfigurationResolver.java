package ink.windlively.dataflow.core.node.resolver;


import ink.windlively.dataflow.core.EnvironmentContext;
import ink.windlively.dataflow.core.SourceEntity;
import ink.windlively.dataflow.core.SpringELExpressionService;
import ink.windlively.dataflow.core.TransferEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Objects;

/**
 * 默认的配置解析器实现, 强依赖于{@link SpringELExpressionService}表达式解析服务
 */
public abstract class DefaultConfigurationResolver implements ConfigurationResolver<Void> {

    public DefaultConfigurationResolver(SpringELExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    public abstract String getName();

    private final ThreadLocal<EnvironmentContext> environmentContext = new ThreadLocal<>();

    public final SpringELExpressionService expressionService;

    @Override
    public void setEnvironmentContext(EnvironmentContext environmentContext) {
        this.environmentContext.set(environmentContext);
    }

    @Nullable
    @Override
    public EnvironmentContext getEnvironmentContext() {
        return environmentContext.get();
    }

    public abstract void resolve(SourceEntity source, TransferEntity input, TransferEntity target, @Nullable Object config,
                                 Map<String, Object> rootData) throws Exception;

    @Override
    public Void resolve(EnvironmentContext env) throws Exception {
        resolve(
                Objects.requireNonNull(env.getVariable("source"), "source entity is null"),
                Objects.requireNonNull(env.getVariable("input"), "input entity is null"),
                Objects.requireNonNull(env.getVariable("target"), "target entity is null"),
                Objects.requireNonNull(env.getVariable("config"), "config is null"),
                Objects.requireNonNull(env.getVariable("root"), "root data is null"));
        return null;
    }

    protected <T> void checkConfigType(Object config, Class<T> expectClass, String expectType){
        Assert.isTrue(expectClass.isInstance(config), getName() + " must be " + expectType + " type");
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DefaultConfigurationResolver &&
                Objects.equals(((DefaultConfigurationResolver) obj).getName(), getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return "DefaultConfigurationResolver(" + getName() + ')';
    }
}
