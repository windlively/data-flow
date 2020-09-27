package ink.andromeda.dataflow.core.node.resolver;

import ink.andromeda.dataflow.core.*;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.Objects;

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

    public abstract void resolve(SourceEntity sourceEntity, TransferEntity transferEntity, Object config,
                                 Map<String, Object> rootData) throws Exception;

    @Override
    public Void resolve(EnvironmentContext env) throws Exception {
        resolve(
                Objects.requireNonNull(env.getVariable("sourceEntity"), "source entity is null"),
                Objects.requireNonNull(env.getVariable("transferEntity"), "transfer entity is null"),
                Objects.requireNonNull(env.getVariable("config"), "config is null"),
                Objects.requireNonNull(env.getVariable("root"), "root data is null")
        );
        return null;
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
}
