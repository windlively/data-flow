package ink.andromeda.dataflow.core.resolver;

import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

public interface ConfigurationResolver<R> {

    <T> R resolve(StandardEvaluationContext context, T object, Map<String, Object> config);

}
