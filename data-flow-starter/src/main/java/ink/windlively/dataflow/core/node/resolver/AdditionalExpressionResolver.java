package ink.windlively.dataflow.core.node.resolver;

import ink.windlively.dataflow.core.SourceEntity;
import ink.windlively.dataflow.core.SpringELExpressionService;
import ink.windlively.dataflow.core.TransferEntity;

import java.util.List;
import java.util.Map;

public class AdditionalExpressionResolver extends DefaultConfigurationResolver{

    public AdditionalExpressionResolver(SpringELExpressionService expressionService) {
        super(expressionService);
    }

    @Override
    public String getName() {
        return "additional_expression";
    }

    @Override
    public void resolve(SourceEntity source, TransferEntity input, TransferEntity target, Object config, Map<String, Object> rootData) throws Exception {
        if(config == null) return;
        checkConfigType(config, List.class, "List<String>");
        //noinspection unchecked
        ((List<String>) config).forEach(expressionService::executeExpression);
    }
}
