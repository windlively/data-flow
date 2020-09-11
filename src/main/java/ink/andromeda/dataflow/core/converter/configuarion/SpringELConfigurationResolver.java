package ink.andromeda.dataflow.core.converter.configuarion;

import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.TransferEntity;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Objects;

public abstract class SpringELConfigurationResolver implements ConfigurationResolver {

    public abstract String getName();

    public abstract void resolve(SourceEntity sourceEntity, TransferEntity transferEntity, Object config,
                                 StandardEvaluationContext context, ExpressionParser parser) throws Exception;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SpringELConfigurationResolver &&
               Objects.equals(((SpringELConfigurationResolver) obj).getName(), getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
