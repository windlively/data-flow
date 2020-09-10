package ink.andromeda.dataflow.core.converter.resolver;

import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.TransferEntity;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public interface SpringELConfigurationResolver<T> {

    String getKey();

    void resolve(SourceEntity sourceEntity, TransferEntity transferEntity, T config,
                 StandardEvaluationContext context, ExpressionParser parser);

}
