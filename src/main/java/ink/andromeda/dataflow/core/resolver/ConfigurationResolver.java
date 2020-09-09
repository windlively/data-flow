package ink.andromeda.dataflow.core.resolver;

import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

public interface ConfigurationResolver {

    <T> void resolve(StandardEvaluationContext context, T object, Map<String, Object> config);

}
